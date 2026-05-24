# Vocal Removal Model Experiments

This project now supports runtime model selection for vocal removal.

## Priority 1: MDX-Net checkpoints

1. Put a candidate model at:
   - `media/src/main/assets/UVR_MDXNET_1_9703.onnx`
2. Open Convert screen -> Vocal Removal section -> AI Model.
3. Select `MDX Alt A (UVR_MDXNET_1_9703) [MDX-Net]`.
4. Verify logs:
   - `ONNX session created with NNAPI for ...`
   - no `ONNX inference failed`
   - low `starvedTransitions`

Low-latency comparison slot:

- `MDX Alt B (Inst_Main_T32) [MDX-Net]` uses fixed `T=32` and lower algorithmic delay.

## Priority 2: TFC-TDF-UNet v3 experimental slot

1. Put experimental model at:
   - `media/src/main/assets/TFC-TDF-UNet-v3-Inst.onnx`
2. Select `TFC-TDF v3 (Experimental, needs conversion) [TFC-TDF-UNet]`.
3. Current app validates model I/O shape compatibility before running.
   - Required: MDX-like rank4 shape compatible with profile expectation.
4. If rejected, check logs for shape mismatch and convert/export model accordingly.

## Notes

- The app now supports per-profile `(dimF, T)` shape expectations.
- Model switch failure triggers automatic revert to previous model profile.
