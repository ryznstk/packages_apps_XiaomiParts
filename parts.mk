# Sepolicy
BOARD_VENDOR_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy/vendor
SYSTEM_EXT_PUBLIC_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy/public
SYSTEM_EXT_PRIVATE_SEPOLICY_DIRS += packages/apps/XiaomiParts/sepolicy/private

# Soong
PRODUCT_SOONG_NAMESPACES += \
    packages/apps/XiaomiParts

ifeq ($(TARGET_USE_CLEARSPEAKER),true)
PRODUCT_PACKAGES += \
    ClearSpeaker
endif
ifeq ($(TARGET_USE_SATURATIONSLIDER),true)
PRODUCT_PACKAGES += \
    Saturation
endif
ifeq ($(TARGET_USE_DCDIMMING),true)
PRODUCT_PACKAGES += \
    DcDimming
endif
ifeq ($(TARGET_USE_KPROFILES),true)
PRODUCT_PACKAGES += \
    KProfiles
endif
ifeq ($(TARGET_SUPPORTS_EDGEREJECTION),true)
PRODUCT_PACKAGES += \
    PanelOrientation
endif
ifeq ($(TARGET_SUPPORTS_HTSR),true)
PRODUCT_PACKAGES += \
    TouchSamplingRate
endif
ifeq ($(TARGET_USE_THERMALPROFILES),true)
PRODUCT_PACKAGES += \
    ThermalProfiles
endif
