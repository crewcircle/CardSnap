import { parseContactInfo, ContactInfo } from "../src/utils/contactParser";

describe("parseContactInfo", () => {
  test("should extract email from text", () => {
    const text = "John Doe\nEmail: john.doe@example.com\nPhone: 555-1234";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.email).toBe("john.doe@example.com");
  });

  test("should extract phone number from text", () => {
    const text = "John Doe\nEmail: john@example.com\nPhone: +1-555-123-4567";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.phone).toBe("+1-555-123-4567");
  });

  test("should extract company name from text", () => {
    const text = "John Doe\nAcme Inc.\njohn@example.com";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.company).toBe("Acme Inc.");
  });

  test("should extract name from first line", () => {
    const text = "John Doe\njohn@example.com\n555-1234";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.name).toBe("John Doe");
  });

  test("should extract website from text", () => {
    const text = "John Doe\njohn@example.com\nWebsite: https://example.com";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.website).toBe("https://example.com");
  });

  test("should handle empty text", () => {
    const text = "";
    const result: ContactInfo = parseContactInfo(text);
    expect(result).toEqual({});
  });

  test("should handle text with no contact info", () => {
    const text = "This is just some random text\nWith no contact information";
    const result: ContactInfo = parseContactInfo(text);
    expect(result).toEqual({});
  });

  test("should extract all contact info from complex text", () => {
    const text =
      "Jane Smith\nSenior Developer\nTechCorp Solutions\nEmail: jane.smith@techcorp.com\nPhone: +1-555-987-6543\nWebsite: https://techcorp.com\nAddress: 123 Tech Street, San Francisco, CA";
    const result: ContactInfo = parseContactInfo(text);
    expect(result.name).toBe("Jane Smith");
    expect(result.company).toBe("TechCorp Solutions");
    expect(result.email).toBe("jane.smith@techcorp.com");
    expect(result.phone).toBe("+1-555-987-6543");
    expect(result.website).toBe("https://techcorp.com");
    expect(result.address).toBeUndefined(); // Address parsing not implemented
  });
});
