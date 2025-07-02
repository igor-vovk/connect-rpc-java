# This Makefile orchestrates the build and test process for the conformance tests.
# It builds the Java application on the host, and then runs the tests in a Docker container
# by mounting the built application into a pre-built conformance image.

DOCKER_BUILD_CMD = docker build -f conformance-build/Dockerfile . --progress=plain --output out/

.PHONY: all
all: test-conformance-all

.PHONY: fmt
fmt:
	./gradlew spotlessApply

.PHONY: build
build:
	./gradlew clean :conformance:build
	@rm -rf build/conformance-1.0.0
	@unzip -oq conformance/build/distributions/conformance-1.0.0.zip -d build

.PHONY: test-conformance-all
test-conformance-all: build
	@echo "Running all conformance tests..."
	make test-conformance profile=netty-server
	make test-conformance profile=netty-client

.PHONY: test-conformance
test-conformance: build
	@echo "Running conformance tests with profile: $(profile)"
ifeq ($(profile),netty-client)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-client.yaml --build-arg launcher=netty-client --build-arg mode=client
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code
else ifeq ($(profile),netty-server)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty.yaml --build-arg launcher=netty-server --build-arg parallel_args="--parallel 1"
	@code=$$(cat out/exit_code | tr -d '\n'); echo "Exiting with code: $$code"; exit $$code
else ifeq ($(profile),netty-server-nonstable)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-nonstable.yaml --build-arg launcher=netty-server --build-arg parallel_args="--parallel 1"
else
	@echo "Error: Unknown profile '$(profile)'. Supported profiles: netty-client, netty-server, netty-server-nonstable."
	@exit 1
endif