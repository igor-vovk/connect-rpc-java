.PHONY: test-conformance

# Define common variables
# To specify a profile, use: make test-conformance profile=<profile_name>
DOCKER_BUILD_CMD = docker build -f conformance-build/Dockerfile . --progress=plain --output "out"

test-conformance:
	@echo "Running conformance tests with profile: $(profile)"
ifeq ($(profile),netty-client)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-client.yaml --build-arg launcher=netty-client --build-arg mode=client
else ifeq ($(profile),netty-server)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty.yaml --build-arg launcher=netty-server
else ifeq ($(profile),netty-server-nonstable)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-nonstable.yaml --build-arg launcher=netty-server --build-arg stable=false
else
	@echo "Error: Unknown profile '$(profile)'. Supported profiles: netty-client, netty-server, netty-server-nonstable."
	@exit 1
endif
