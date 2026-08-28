--[[
  Golden Path Lightroom export provider (Lr* SDK only).
]]

local LrLogger = import "LrLogger"

local log = LrLogger("GoldenPathExport")
log:enable("logfile")

return {
  hideSections = { "exportLocation" },
  allowFileFormats = { "JPEG", "TIFF" },
  allowColorSpaces = { "sRGB" },
  exportPresetFields = {},
  processRenderedPhotos = function(_functionContext, exportContext)
    local exportSession = exportContext.exportSession
    if exportSession:countRenditions() < 1 then
      return
    end
    for _, rendition in exportSession:renditions() do
      local success, pathOrMessage = rendition:waitForRender()
      if success then
        log:info("Golden Path export wrote " .. tostring(pathOrMessage))
      end
    end
  end,
}
