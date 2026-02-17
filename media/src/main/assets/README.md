Place ONNX model files in this directory.

Current selectable profiles in app:

- `UVR-MDX-NET-Inst_Main_fp16_dynT.onnx` (MDX Main, default)
- `UVR-MDX-NET_Main_390.onnx` (MDX Alt A, test slot)
- `TFC-TDF-UNet-v3-Inst.onnx` (TFC-TDF v3 experimental slot, currently unconverted waveform ONNX)

Important:

- Real-time path currently supports models with MDX-like I/O shape rank 4 and channels/freq compatible with `[1, 4, 2048, T]`.
- App now reads per-profile expected shape (example: Main_390 uses `[1,4,3072,256]`).
- If a model file is missing or shape is incompatible, loading is rejected and logs will include the reason.
