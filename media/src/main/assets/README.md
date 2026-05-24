Place only release-bundled vocal separation runtime assets in this directory.

Runtime asset:

- `vocal_separation_core.bin`

Release rule:

- Keep experimental candidates outside `media/src/main/assets`.
- Files in this directory are packaged into the app.
- Default path expects an MDX-style rank 4 shape compatible with `[1, 4, 2048, 32]`.
- If the runtime asset is missing or shape is incompatible, loading is rejected and logs include the reason.
