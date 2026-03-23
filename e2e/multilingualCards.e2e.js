describe("Multilingual Business Card E2E Tests", () => {
  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it("should handle business cards with Japanese text (vertical layout)", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    // Simulate extracting Japanese business card information
    await expect(element(by.id("extracted-text"))).toHaveText(/田中 太郎/i);
    await expect(element(by.id("extracted-text"))).toHaveText(/株式会社例示/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /tanaka@example\.co\.jp/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-田中 太郎"))).toBeVisible();
  });

  it("should handle business cards with Arabic text (right-to-left layout)", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    // Simulate extracting Arabic business card information
    await expect(element(by.id("extracted-text"))).toHaveText(/محمد أحمد/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /شركة المثال المحدودة/i
    );
    await expect(element(by.id("extracted-text"))).toHaveText(
      /m\.ahmed@example\.sa/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-محمد أحمد"))).toBeVisible();
  });

  it("should handle bilingual business cards (English/Spanish)", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    // Simulate extracting bilingual business card information
    await expect(element(by.id("extracted-text"))).toHaveText(/María García/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /Empresa Ejemplo S.A./i
    );
    await expect(element(by.id("extracted-text"))).toHaveText(
      /mgarcia@example\.es/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-María García"))).toBeVisible();
  });

  it("should handle business cards with special characters and accents", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    // Simulate extracting business card with special characters
    await expect(element(by.id("extracted-text"))).toHaveText(/Renée Müller/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /Société Exemple/i
    );
    await expect(element(by.id("extracted-text"))).toHaveText(
      /r\.muller@example\.fr/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-Renée Müller"))).toBeVisible();
  });
});
