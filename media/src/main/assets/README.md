Place ONNX model files in this directory.

Current runtime model in app:

- `UVR-MDX-NET-Inst_Main_fp16_dynT.onnx` (default)

Important:

- Real-time path expects MDX-like I/O shape rank 4 with `[1, 4, 2048, 32]` (dynamic dimension allowed).
- If the model file is missing or shape is incompatible, loading is rejected and logs include the reason.
