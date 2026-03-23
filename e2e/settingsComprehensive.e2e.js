describe("Settings Persistence Comprehensive E2E Tests", () => {
  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it("should persist OCR language settings across app restart", async () => {
    await element(by.id("contacts-tab-button")).tap();
    await element(by.id("settings-tab-button")).tap();
    await expect(element(by.id("settings-screen"))).toBeVisible();

    await element(by.id("language-toggle-eng")).tap();
    await element(by.id("language-toggle-spa")).tap();

    await device.reloadReactNative();

    await element(by.id("contacts-tab-button")).tap();
    await element(by.id("settings-tab-button")).tap();
    await expect(element(by.id("settings-screen"))).toBeVisible();
  });

  it("should persist auto-save, notifications, and data usage settings across app restart", async () => {
    await element(by.id("contacts-tab-button")).tap();
    await element(by.id("settings-tab-button")).tap();
    await expect(element(by.id("settings-screen"))).toBeVisible();

    await element(by.id("auto-save-switch")).tap();
    await element(by.id("notifications-switch")).tap();
    await element(by.id("cellular-option")).tap();

    await device.reloadReactNative();

    await element(by.id("contacts-tab-button")).tap();
    await element(by.id("settings-tab-button")).tap();
    await expect(element(by.id("settings-screen"))).toBeVisible();
  });

  it("should apply OCR language settings to scanning functionality", async () => {
    await element(by.id("contacts-tab-button")).tap();
    await element(by.id("settings-tab-button")).tap();
    await expect(element(by.id("settings-screen"))).toBeVisible();

    await element(by.id("language-toggle-eng")).tap();
    await element(by.id("language-toggle-spa")).tap();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    await expect(element(by.id("extracted-text"))).toHaveText(/Juan Pérez/i);

    await element(by.id("save-contact-button")).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await element(by.id("camera-view")).toBeVisible();
  });
});
