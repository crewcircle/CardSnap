import storageUtils from "../src/utils/storage";

describe("storageUtils", () => {
  beforeEach(() => {
    // Clear storage before each test
    storageUtils.saveContacts([]);
    storageUtils.saveSetting("testSetting", null);
    storageUtils.saveOcrLanguages(["eng"]);
  });

  test("should save and get contacts", () => {
    const testContact = {
      id: "1",
      name: "John Doe",
      email: "john@example.com",
      phone: "123-456-7890",
      company: "Acme Inc",
      address: "123 Main St",
      website: "https://example.com",
      scannedAt: new Date().toISOString(),
    };

    // Save contact
    storageUtils.addContact(testContact);

    // Get contacts
    const contacts = storageUtils.getContacts();
    expect(contacts.length).toBe(1);
    expect(contacts[0]).toMatchObject(testContact);
  });

  test("should delete contact", () => {
    const testContact = {
      id: "1",
      name: "John Doe",
      email: "john@example.com",
      phone: "123-456-7890",
      company: "Acme Inc",
      address: "",
      website: "",
      scannedAt: new Date().toISOString(),
    };

    storageUtils.addContact(testContact);
    let contacts = storageUtils.getContacts();
    expect(contacts.length).toBe(1);

    storageUtils.deleteContact("1");
    contacts = storageUtils.getContacts();
    expect(contacts.length).toBe(0);
  });

  test("should update contact", () => {
    const testContact = {
      id: "1",
      name: "John Doe",
      email: "john@example.com",
      phone: "123-456-7890",
      company: "Acme Inc",
      address: "",
      website: "",
      scannedAt: new Date().toISOString(),
    };

    storageUtils.addContact(testContact);

    const updatedContact = {
      ...testContact,
      name: "Jane Doe",
      email: "jane@example.com",
    };

    storageUtils.updateContact("1", updatedContact);
    const contacts = storageUtils.getContacts();
    expect(contacts[0].name).toBe("Jane Doe");
    expect(contacts[0].email).toBe("jane@example.com");
  });

  test("should handle settings", () => {
    const testValue = { theme: "dark", notifications: true };
    storageUtils.saveSetting("testSetting", testValue);
    const retrieved = storageUtils.getSetting("testSetting");
    expect(retrieved).toEqual(testValue);
  });

  test("should handle OCR languages", () => {
    const languages = ["eng", "spa", "fra"];
    storageUtils.saveOcrLanguages(languages);
    const retrieved = storageUtils.getOcrLanguages();
    expect(retrieved).toEqual(languages);
  });
});
