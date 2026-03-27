import React from "react";
import { Alert } from "react-native";
import {
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react-native";
import EditContactScreen from "./EditContactScreen";
import { Contact, createContact } from "../types/contact";
import storageUtils from "../utils/storage";
import { showErrorAlert } from "../utils/errorHandler";

jest.mock("../utils/storage", () => ({
  __esModule: true,
  default: {
    getContacts: jest.fn(),
    updateContact: jest.fn(),
    deleteContact: jest.fn(),
  },
}));

jest.mock("../utils/errorHandler", () => ({
  __esModule: true,
  showErrorAlert: jest.fn(),
}));

type EditContactScreenProps = React.ComponentProps<typeof EditContactScreen>;

const mockedStorageUtils = storageUtils as jest.Mocked<typeof storageUtils>;
const mockedShowErrorAlert = showErrorAlert as jest.MockedFunction<
  typeof showErrorAlert
>;
const mockedAlert = Alert.alert as jest.MockedFunction<typeof Alert.alert>;
const mockGoBack = jest.fn();
let consoleWarnSpy: jest.SpyInstance;

const contact: Contact = createContact(
  {
    name: "Jane Doe",
    email: "jane@example.com",
    phone: "098-765-4321",
    company: "XYZ Corp",
    address: "456 Oak Ave",
    website: "https://xyz.com",
  },
  {
    id: "contact-1",
    scannedAt: "2026-03-24T00:00:00.000Z",
  }
);

const createProps = (): EditContactScreenProps => {
  return {
    route: {
      key: "EditContact-contact-1",
      name: "EditContact",
      params: { contactId: "contact-1" },
    },
    navigation: {
      goBack: mockGoBack,
    } as unknown as EditContactScreenProps["navigation"],
  };
};

describe("EditContactScreen", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    consoleWarnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

    mockedStorageUtils.getContacts.mockResolvedValue([contact]);
    mockedStorageUtils.updateContact.mockResolvedValue(contact);
    mockedStorageUtils.deleteContact.mockResolvedValue();
  });

  afterEach(() => {
    consoleWarnSpy.mockRestore();
  });

  it("loads and displays the selected contact", async () => {
    render(<EditContactScreen {...createProps()} />);

    await waitFor(() => {
      const nameInput = screen.getByTestId("name-input");
      const emailInput = screen.getByTestId("email-input");
      const phoneInput = screen.getByTestId("phone-input");
      const companyInput = screen.getByTestId("company-input");
      
      expect((nameInput as unknown as { props: { value: string } }).props.value).toBe("Jane Doe");
      expect((emailInput as unknown as { props: { value: string } }).props.value).toBe("jane@example.com");
      expect((phoneInput as unknown as { props: { value: string } }).props.value).toBe("098-765-4321");
      expect((companyInput as unknown as { props: { value: string } }).props.value).toBe("XYZ Corp");
    });
  }, 10000);

  it("saves edits while preserving contact identity fields", async () => {
    render(<EditContactScreen {...createProps()} />);

    const nameInput = await screen.findByTestId("name-input");
    fireEvent.changeText(nameInput, "Jane Smith");
    fireEvent.press(screen.getByTestId("save-button"));

    await waitFor(() => {
      expect(mockedStorageUtils.updateContact).toHaveBeenCalledTimes(1);
    });

    expect(mockedStorageUtils.updateContact).toHaveBeenCalledWith(
      "contact-1",
      expect.objectContaining({
        id: "contact-1",
        scannedAt: "2026-03-24T00:00:00.000Z",
        name: "Jane Smith",
        updatedAt: expect.any(String),
      })
    );
    expect(mockedAlert).toHaveBeenCalledWith(
      "Success",
      "Contact updated successfully!"
    );
    expect(mockGoBack).toHaveBeenCalled();
  });

  it("shows an error and navigates back when the contact does not exist", async () => {
    mockedStorageUtils.getContacts.mockResolvedValueOnce([]);

    render(<EditContactScreen {...createProps()} />);

    await waitFor(() => {
      expect(mockedAlert).toHaveBeenCalledWith("Error", "Contact not found");
    });
    expect(mockGoBack).toHaveBeenCalled();
  });

  it("prevents saving when the name is empty", async () => {
    render(<EditContactScreen {...createProps()} />);

    const nameInput = await screen.findByTestId("name-input");
    fireEvent.changeText(nameInput, "   ");
    fireEvent.press(screen.getByTestId("save-button"));

    expect(mockedAlert).toHaveBeenCalledWith("Error", "Name is required");
    expect(mockedStorageUtils.updateContact).not.toHaveBeenCalled();
  });

  it("deletes the contact after confirmation", async () => {
    render(<EditContactScreen {...createProps()} />);

    await screen.findByTestId("name-input");
    fireEvent.press(screen.getByTestId("delete-button"));

    const deleteAlertCall = mockedAlert.mock.calls.find(
      ([title]) => title === "Delete Contact"
    );
    const buttons = deleteAlertCall?.[2] as
      | Array<{ text?: string; onPress?: () => void | Promise<void> }>
      | undefined;
    const deleteButton = buttons?.find((button) => button.text === "Delete");

    expect(deleteButton).toBeTruthy();

    await deleteButton?.onPress?.();

    expect(mockedStorageUtils.deleteContact).toHaveBeenCalledWith("contact-1");
    expect(mockGoBack).toHaveBeenCalled();
  });

  it("routes load failures through the shared error handler", async () => {
    mockedStorageUtils.getContacts.mockRejectedValueOnce(
      new Error("Load failed")
    );

    render(<EditContactScreen {...createProps()} />);

    await waitFor(() => {
      expect(mockedShowErrorAlert).toHaveBeenCalledWith(
        expect.objectContaining({ message: "Load failed" }),
        "Load contact"
      );
    });
  });
});
