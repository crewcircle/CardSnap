import React from "react";
import { Alert } from "react-native";
import {
  act,
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react-native";
import ContactsScreen from "./ContactsScreen";
import { Contact, createContact } from "../types/contact";
import storageUtils from "../utils/storage";
import { exportContactsAsCSV } from "../utils/exportUtils";
import { showErrorAlert } from "../utils/errorHandler";

const mockNavigate = jest.fn();

jest.mock("@react-navigation/native", () => {
  const React = require("react");
  return {
    useNavigation: () => ({
      navigate: mockNavigate,
    }),
    useFocusEffect: (callback: () => void | (() => void)) => {
      React.useEffect(() => {
        const cleanup = callback();
        return cleanup;
      }, [callback]);
    },
  };
});

jest.mock("../utils/storage", () => ({
  __esModule: true,
  default: {
    getContacts: jest.fn(),
    deleteContact: jest.fn(),
  },
}));

jest.mock("../utils/exportUtils", () => ({
  __esModule: true,
  exportContactsAsCSV: jest.fn(),
}));

jest.mock("../utils/errorHandler", () => ({
  __esModule: true,
  showErrorAlert: jest.fn(),
}));

const mockedStorageUtils = storageUtils as jest.Mocked<typeof storageUtils>;
const mockedExportContactsAsCSV = exportContactsAsCSV as jest.MockedFunction<
  typeof exportContactsAsCSV
>;
const mockedShowErrorAlert = showErrorAlert as jest.MockedFunction<
  typeof showErrorAlert
>;
const mockedAlert = Alert.alert as jest.MockedFunction<typeof Alert.alert>;
let consoleWarnSpy: jest.SpyInstance;

const contacts: Contact[] = [
  createContact(
    {
      name: "John Doe",
      email: "john@example.com",
      phone: "123-456-7890",
      company: "Acme Inc",
    },
    {
      id: "contact-1",
      scannedAt: "2026-03-24T00:00:00.000Z",
    }
  ),
];

describe("ContactsScreen", () => {
  beforeEach(() => {
    jest.clearAllMocks();
    consoleWarnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

    mockedStorageUtils.getContacts.mockResolvedValue([]);
    mockedStorageUtils.deleteContact.mockResolvedValue();
    mockedExportContactsAsCSV.mockResolvedValue();
  });

  afterEach(() => {
    consoleWarnSpy.mockRestore();
  });

  it("shows loading state before contacts are loaded", () => {
    mockedStorageUtils.getContacts.mockImplementation(
      () =>
        new Promise(() => {
          return;
        })
    );

    render(<ContactsScreen />);

    expect(screen.getByText("Loading contacts...")).toBeTruthy();
  });

  it("renders contacts from storage", async () => {
    mockedStorageUtils.getContacts.mockResolvedValueOnce(contacts);

    render(<ContactsScreen />);

    expect(await screen.findByText("John Doe")).toBeTruthy();
    expect(screen.getByText(/john@example\.com/i)).toBeTruthy();
    expect(screen.getByText(/123-456-7890/i)).toBeTruthy();
    expect(screen.getByText(/Acme Inc/i)).toBeTruthy();
  });

  it("shows an empty state when no contacts exist", async () => {
    render(<ContactsScreen />);

    expect(await screen.findByText(/No contacts yet/i)).toBeTruthy();
  });

  it("navigates to edit when a contact row is pressed", async () => {
    mockedStorageUtils.getContacts.mockResolvedValueOnce(contacts);

    render(<ContactsScreen />);

    const contactRow = await screen.findByTestId("contact-item-contact-1");
    fireEvent.press(contactRow);

    expect(mockNavigate).toHaveBeenCalledWith("EditContact", {
      contactId: "contact-1",
    });
  });

  it("exports all contacts from the header action", async () => {
    mockedStorageUtils.getContacts.mockResolvedValueOnce(contacts);

    render(<ContactsScreen />);

    await screen.findByText("John Doe");
    fireEvent.press(screen.getByTestId("export-all-contacts-button"));

    await waitFor(() => {
      expect(mockedExportContactsAsCSV).toHaveBeenCalledWith(contacts);
    });
    expect(mockedAlert).toHaveBeenCalledWith(
      "Success",
      "All contacts exported as CSV!"
    );
  });

  it("shows an info alert when export is pressed with no contacts", async () => {
    render(<ContactsScreen />);

    await screen.findByText(/No contacts yet/i);
    fireEvent.press(screen.getByTestId("export-all-contacts-button"));

    expect(mockedAlert).toHaveBeenCalledWith("Info", "No contacts to export");
  });

  it("routes export failures through the shared error handler", async () => {
    mockedStorageUtils.getContacts.mockResolvedValueOnce(contacts);
    mockedExportContactsAsCSV.mockRejectedValueOnce(new Error("Export failed"));

    render(<ContactsScreen />);

    await screen.findByText("John Doe");
    fireEvent.press(screen.getByTestId("export-all-contacts-button"));

    await waitFor(() => {
      expect(mockedShowErrorAlert).toHaveBeenCalledWith(
        expect.objectContaining({ message: "Export failed" }),
        "Export contacts"
      );
    });
  });

  it("deletes a contact after confirmation", async () => {
    mockedStorageUtils.getContacts.mockResolvedValue(contacts);

    render(<ContactsScreen />);

    await screen.findByText("John Doe");
    const deleteButtonNode = screen.getByTestId("delete-button-contact-1");
    await act(async () => {
      fireEvent(deleteButtonNode, "press", {
        stopPropagation: jest.fn(),
      });
    });

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
    expect(mockedStorageUtils.getContacts).toHaveBeenCalledTimes(2);
  });
});
