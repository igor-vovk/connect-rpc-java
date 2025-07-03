# Example of a client-server communication using ConnectRPC as a protocol.

This example demonstrates how to set up a simple client-server communication using ConnectRPC as the protocol.

Normally, in a real-world application, protobuf files are put in a separate module, that is built as a library,
and then used by both the client and the server code. In this example, for simplicity, the protobuf files, client, and
server code are all in the same module.