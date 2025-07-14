# This Makefile orchestrates the build and test process for the conformance tests.
# It builds the Java application on the host, and then runs the tests in a Docker container
# by mounting the built application into a pre-built conformance image.

DOCKER_BUILD_CMD = docker build -f conformance-build/Dockerfile . --progress=plain --output out/

.PHONY: fmt
fmt:
	./gradlew spotlessApply

.PHONY: build-conformance
build-conformance:
	./gradlew :conformance:build
	@rm -rf build/conformance-1.0.0
	@unzip -oq conformance/build/distributions/conformance-1.0.0.zip -d build

.PHONY: test-conformance
test-conformance: build-conformance
ifeq ($(profile),netty-client)
	@echo "Running conformance tests with profile: $(profile)"
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-client.yaml --build-arg launcher=netty-client --build-arg mode=client
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code
else ifeq ($(profile),netty-server)
	@echo "Running conformance tests with profile: $(profile)"
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty.yaml --build-arg launcher=netty-server --build-arg parallel_args="--parallel 1"
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code
else ifeq ($(profile),netty-server-nonstable)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-nonstable.yaml --build-arg launcher=netty-server --build-arg parallel_args="--parallel 1"
else ifeq ($(profile),)
	@echo "Running all stable conformance tests..."
	make test-conformance profile=netty-server
	make test-conformance profile=netty-client
else
	$(error Invalid profile: $(profile). Use netty-client, netty-server, or netty-server-nonstable.)
endif