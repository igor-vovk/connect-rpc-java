package me.ivovk.connect_rpc_java.netty;

import io.grpc.Metadata;
import io.netty.handler.codec.http.DefaultHttpHeaders;
import io.netty.handler.codec.http.HttpHeaders;
import me.ivovk.connect_rpc_java.core.http.HeaderMapping;

import java.util.function.Predicate;

public class NettyHeaderMapping implements HeaderMapping<HttpHeaders> {

  private final Predicate<String> headersFilter;
  private final Predicate<String> metadataFilter;
  private final Boolean treatTrailersAsHeaders;

  public NettyHeaderMapping(
      Predicate<String> headersFilter,
      Predicate<String> metadataFilter,
      Boolean treatTrailersAsHeaders) {
    this.headersFilter = headersFilter;
    this.metadataFilter = metadataFilter;
    this.treatTrailersAsHeaders = treatTrailersAsHeaders;
  }

  @Override
  public Metadata toMetadata(HttpHeaders headers) {
    var metadata = new Metadata();

    for (var h : headers) {
      var name = h.getKey();
      var value = h.getValue();

      if (headersFilter.test(name)) {
        metadata.put(HeaderMapping.metadataKeyByHeaderName(name), value);
      }
    }

    return metadata;
  }

  private HttpHeaders headers(Metadata metadata, Boolean trailing) {
    var headers = new DefaultHttpHeaders();

    for (var k : metadata.keys()) {
      if (metadataFilter.test(k)) {
        var name = trailing ? "trailer-" + k : k;

        headers.add(name, metadata.getAll(HeaderMapping.cachedAsciiKey(name)));
      }
    }

    return headers;
  }

  @Override
  public HttpHeaders toHeaders(Metadata metadata) {
    return headers(metadata, false);
  }

  @Override
  public HttpHeaders trailersToHeaders(Metadata trailers) {
    boolean trailing = !treatTrailersAsHeaders;

    return headers(trailers, trailing);
  }
}
