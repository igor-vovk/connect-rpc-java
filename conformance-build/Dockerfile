FROM eclipse-temurin:17-jre AS base

FROM ghcr.io/igor-vovk/connect-conformance-dockerimage:1.0.4-1 AS conformance

FROM base AS runner

COPY --from=conformance /conformance /conformance
ADD conformance-build/*.yaml /conformance/
ADD build/conformance-1.0.0 /app
WORKDIR /conformance

RUN mkdir "/out"

ARG config="suite.yaml"
ARG mode="server"
ARG launcher
ARG parallel_args=""

# Run stable tests first
RUN LOGS_PATH="/out" \
    ./connectconformance \
    --conf $config \
    --mode $mode \
    $parallel_args \
    -v -vv --trace \
    -- \
    /app/bin/conformance $launcher; \
    echo $? > /out/exit_code

FROM scratch AS export

COPY --link --from=runner /out /
