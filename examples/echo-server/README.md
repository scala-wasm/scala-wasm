# WASI echo-server example

`wasmtime serve` and low-level container runtime for Wasm like [runwasi](https://github.com/containerd/runwasi) uses the [wasi:http/proxy](https://github.com/WebAssembly/wasi-http/blob/main/wit/proxy.wit) world, which is a world just for accepting requests and sending back the responses.

This is a simple example to run WASI HTTP server using `wasmtime serve` command.

```sh
sbt:Scala.js> set Global/enableWasmEverywhere := true
sbt:Scala.js> echoserver2_12/fastLinkJS
[info] Fast optimizing .../scala-wasm/examples/echo-server/.2.12/target/scala-2.12/echo-fastopt
```

This produces the [Wasm Component Model compliant Wasm module binary](https://github.com/WebAssembly/component-model/pull/378). We can translate the Wasm module to Wasm component binary using `wasm-tools`.

```sh
$ cd examples/echo-server
$ wasm-tools component embed wit .2.12/target/scala-2.12/echo-fastopt/main.wasm -o main.wasm --encoding utf16
$ wasm-tools component new main.wasm -o main.wasm
```

Run the server using `wasmtime serve` command. Adding `-C collector=null` to disable garbage collection in wasmtime, since it causes some issues with component model.

```sh
$ wasmtime serve -W function-references,gc -C collector=null main.wasm
Serving HTTP on http://0.0.0.0:8080/
```

```sh
$ curl http://localhost:8080 -d 'hello world!'
hello world!
```

## Deploy to k8s

You can [push Wasm components to OCI registries](https://opensource.microsoft.com/blog/2024/09/25/distributing-webassembly-components-using-oci-registries/) like GitHub Container Registry or Docker Hub, and can run Wasm workloads directly without Linux containers, which results in extremely fast cold startup times.

```sh
$ wkg oci push ghcr.io/<namespace>/<image>:<tag> main.wasm
```

The following instructions explain how to deploy Wasm workloads to a local Kubernetes cluster using [kind](https://kind.sigs.k8s.io/), based on the [runwasi quickstart tutorial](https://runwasi.dev/getting-started/quickstart.html).

As a workaround for [issue #1004](https://github.com/containerd/runwasi/issues/1004), you must install a specific build of `containerd-shim-wasmtime` from [this release](https://github.com/scala-wasm/runwasi/releases/tag/containerd-shim-wasmtime%2Fv0.6.0-wasmgc), which enables `wasm_gc` and `function_references` or build it yourself from the [main branch](https://github.com/scala-wasm/runwasi).
This binary is target for `aarch64-unknown-linux-musl` binary, if you need `x86-64` or `gnu`, please build yourself.

```yaml
# kind-config.yaml
kind: Cluster
apiVersion: kind.x-k8s.io/v1alpha4
name: runwasi-cluster
nodes:
- role: control-plane
  extraMounts:
    # path to the containerd-shim-wasmtime-v1 binary in host
  - hostPath: /usr/local/bin/containerd-shim-wasmtime-v1
    containerPath: /usr/local/bin/containerd-shim-wasmtime-v1
```

```sh
$ kind create cluster --name runwasi-cluster --config kind-config.yaml

# configure containerd
$ cat << EOF | docker exec -i runwasi-cluster-control-plane tee /etc/containerd/config.toml
[plugins."io.containerd.cri.v1.runtime".containerd.runtimes.wasm]
  runtime_type = "io.containerd.wasmtime.v1"
EOF

$ docker exec runwasi-cluster-control-plane systemctl restart containerd
```

**Deploy Wasm workload**

```yaml
# deploy.yaml
apiVersion: node.k8s.io/v1
kind: RuntimeClass
metadata:
  name: wasm
handler: wasm
---
apiVersion: apps/v1
kind: Deployment
metadata:
  name: wasi-demo
spec:
  replicas: 1
  selector:
    matchLabels:
      app: wasi-demo
  template:
    metadata:
      labels:
        app: wasi-demo
    spec:
      runtimeClassName: wasm
      containers:
      - name: demo
        image: ghcr.io/tanishiking/scala-wasm-echo-server:latest
        command: [""]
```

```sh
$ kubectl --context kind-runwasi-cluster apply -f deploy.yaml

$ kubectl port-forward deployment/wasi-demo 8080:8080
$ curl http://localhost:8080 -d 'hello wasi:http/proxy!'
hello wasi:http/proxy!
```
