# Envoy WebSocket ext_proc test server

This project consists of three components:

- Envoy config to route WebSocket traffic through an ext_proc filter
- A gRPC server implementing the ext_proc contract that will reject any messages sent on the WS stream
- A mock WebSockets ticker server

## Running the server

The ext_proc server runs on port 9000.

```shell 
./mvnw quarkus:dev
```

## Running the WebSocket server

The server runs on port 4545.

```shell
cd mock-server
python -m venv venv/ 
. venv/bin/activate 
pip install -r requirements.txt

python server.py
```

## Running Envoy

This configures Envoy to run on port 8888

I have been running envoy with [`func-e`](http://func-e.io).
Once that is installed:

```shell
cd envoy-config
func-e run -c websocket-extproc.yaml -l debug
```

## Calling the server

I've been using `wscat`, which is cURL for WebSockets.

```shell 
npm install -g wscat
wscat -c ws://localhost:8888
```

Observe the tick messages - but try sending anything and you'll be evicted.