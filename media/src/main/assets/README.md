Place only release-bundled ONNX model files in this directory.

Runtime model:

- `UVR_MDXNET_3_9662_dynT.onnx`

Release rule:

- Keep experimental candidates outside `media/src/main/assets`.
- Files in this directory are packaged into the app.
- Default path expects MDX-like I/O shape rank 4 with `[1, 4, 2048, 32]` (dynamic dimension allowed).
- If the model file is missing or shape is incompatible, loading is rejected and logs include the reason.
