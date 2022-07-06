# routing_service
Goal: Implement a routing service given start and end position (originLat, originLon, destinationLat, destinationLon) by implementing various pathfinding algorithms
Implemented a TCP-based server which:

• Loads a given graph file on startup and wait for requests

• Reads a routing request and writes responses as GeoJsons via TCP.

• Handles requests (compute paths), each in its own thread. In other words, the server listens for new
requests while processing the existing ones.

Implemented two Jersey services for computing the shortest path by calling the server via TCP. Sending a request from
the UI, the Jersey resource connects to the server via TCP and receives a well-formated GeoJson response
which the Jersey can hand on to the UI.

The UI expects to get a GeoJson file for the following two algorithms by the respective URL:

• Dijkstra: http://localhost:9090/sysdev/dijkstra

• A*: http://localhost:9090/sysdev/astar

Start and end position is passed as query parameters (originLat, originLon, destinationLat, destinationLon).
