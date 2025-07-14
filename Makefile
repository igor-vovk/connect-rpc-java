# This Makefile orchestrates the build and test process for the conformance tests.
# It builds the Java application on the host, and then runs the tests in a Docker container
# by mounting the built application into a pre-built conformance image.

DOCKER_BUILD_CMD = docker build -f conformance-build/Dockerfile . --no-cache --progress=plain --output out/

.PHONY: fmt
fmt:
	./gradlew spotlessApply

.PHONY: build-conformance
build-conformance:
	./gradlew :conformance:build
	@rm -rf build/conformance-1.0.0
	@unzip -oq conformance/build/distributions/conformance-1.0.0.zip -d build

.PHONY: test-conformance-netty-server
test-conformance-netty-server: build-conformance
	@echo "Running conformance tests for netty server"
	$(DOCKER_BUILD_CMD) --build-arg launcher=netty-server \
						--build-arg config=suite-netty.yaml \
						--build-arg parallel_args="--parallel 1"
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code

.PHONY: test-conformance-netty-client
test-conformance-netty-client: build-conformance
	@echo "Running conformance tests for netty client"
	$(DOCKER_BUILD_CMD) --build-arg launcher=netty-client \
						--build-arg config=suite-netty-client.yaml \
						--build-arg mode=client
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code

.PHONY: test-conformance-netty-server-nonstable
test-conformance-netty-server-nonstable: build-conformance
	@echo "Running conformance tests for netty server (non-stable)"
	$(DOCKER_BUILD_CMD) --build-arg launcher=netty-server \
						--build-arg config=suite-netty-nonstable.yaml \
						--build-arg parallel_args="--parallel 1"
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code

.PHONY: test-conformance-stable
test-conformance-stable: test-conformance-netty-server \
						 test-conformance-netty-client