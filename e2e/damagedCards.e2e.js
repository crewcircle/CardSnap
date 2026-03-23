describe("Damaged Card & Poor Quality E2E Tests", () => {
  beforeEach(async () => {
    await device.reloadReactNative();
  });

  it("should handle business cards with low contrast or faded text", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    await expect(element(by.id("extracted-text"))).toHaveText(/William Brown/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /wbrown@email\.net/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-William Brown"))).toBeVisible();
  });

  it("should handle business cards with skewed or angled text", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    await expect(element(by.id("extracted-text"))).toHaveText(/Lisa Wilson/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /lisa\.wilson@business\.org/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-Lisa Wilson"))).toBeVisible();
  });

  it("should handle business cards with shadows or uneven lighting", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    await expect(element(by.id("extracted-text"))).toHaveText(/David Chen/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /dchen@company\.cn/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-David Chen"))).toBeVisible();
  });

  it("should handle business cards with text overlapping logos or design elements", async () => {
    await expect(element(by.id("camera-view"))).toBeVisible();
    await element(by.id("capture-button")).tap();

    await expect(element(by.id("results-view"))).toBeVisible();
    await expect(element(by.id("extracted-text"))).toHaveText(/Anna Kowalski/i);
    await expect(element(by.id("extracted-text"))).toHaveText(
      /anna\.k@techpl\.pl/i
    );

    await expect(element(by.id("save-contact-button"))).toBeVisible();
    await element(by.id("save-contact-button")).tap();

    await expect(element(by.id("camera-view"))).toBeVisible();

    await element(by.id("contacts-tab-button")).tap();
    await expect(element(by.id("contacts-screen"))).toBeVisible();

    await expect(element(by.id("contact-item-Anna Kowalski"))).toBeVisible();
  });
});
