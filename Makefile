.PHONY: test-conformance

# Define common variables
DOCKER_BUILD_CMD = docker build -f conformance-build/Dockerfile . --progress=plain

test-conformance:
	@echo "Running conformance tests with profile: $(PROFILE)"
ifeq ($(PROFILE),netty-client)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-client.yaml --build-arg launcher=netty-client --build-arg mode=client
else ifeq ($(PROFILE),netty-server)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty.yaml --build-arg launcher=netty-server
else ifeq ($(PROFILE),netty-server-nonstable)
	$(DOCKER_BUILD_CMD) --build-arg config=suite-netty-nonstable.yaml --build-arg launcher=netty-server --build-arg stable=false
else
	@echo "Error: Unknown profile '$(PROFILE)'. Supported profiles: netty-client, netty-server, netty-server-nonstable."
	@exit 1
endif
