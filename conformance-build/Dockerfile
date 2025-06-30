FROM gradle:8-jdk17 AS base

FROM ghcr.io/igor-vovk/connect-conformance-dockerimage:1.0.4-1 AS conformance

FROM base AS runner

COPY --from=conformance /conformance /conformance
ADD conformance-build/*.yaml /conformance/
ADD build/conformance-1.0.0 /app
WORKDIR /conformance

RUN mkdir "/logs"

ARG config="suite.yaml"
ARG mode="server"
ARG launcher

# Run stable tests first
RUN LOGS_PATH="/logs" \
    ./connectconformance \
    --conf $config \
    --mode $mode \
    -v -vv --trace \
    -- \
    /app/bin/conformance $launcher

FROM scratch AS export

COPY --link --from=runner /logs /
