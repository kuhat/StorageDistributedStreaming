# Reconfiguration Manager

This is the reconfiguration service used to trigger reconfiguration of the application. GRPC protocol is used in this function.

## ReconfigClient

This class extends outputClient and has the client of reconfiguration.

## ResourceMgr

This is the main class used to run the reconfiguration manager. When the application is started, a process is used to run
this class, and reconfigClient is called every 20s to send the reconfiguration messages to the input command server.

