package com.example.media.audio

enum class VocalRemovalModelProfile(
    val assetFileName: String,
    val uiLabel: String,
    val category: String,
    val dimF: Int,
    val targetT: Int
) {
    // Priority 1: MDX-Net family checkpoints (same integration path, lowest risk).
    MDX_MAIN(
        assetFileName = "UVR-MDX-NET-Inst_Main_fp16_dynT.onnx",
        uiLabel = "MDX Main (Current)",
        category = "MDX-Net",
        dimF = 2048,
        targetT = 32
    ),
    MDX_ALT_A(
        assetFileName = "UVR-MDX-NET_Main_390.onnx",
        uiLabel = "MDX Alt A (Main_390)",
        category = "MDX-Net",
        dimF = 3072,
        targetT = 256
    ),

    // Priority 2: TFC-TDF-UNet v3 experimental slot.
    // This app currently expects MDX-style I/O [1,4,2048,T]. If shape differs, load is rejected.
    TFC_TDF_V3_EXP(
        assetFileName = "TFC-TDF-UNet-v3-Inst.onnx",
        uiLabel = "TFC-TDF v3 (Experimental, needs conversion)",
        category = "TFC-TDF-UNet",
        dimF = 2048,
        targetT = 32
    );
}
