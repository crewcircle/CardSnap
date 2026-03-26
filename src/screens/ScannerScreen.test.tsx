import { Alert } from "react-native";
import {
  fireEvent,
  render,
  screen,
  waitFor,
} from "@testing-library/react-native";
import MlkitOcr from "rn-mlkit-ocr";
import { useCameraDevice } from "react-native-vision-camera";
import ScannerScreen from "./ScannerScreen";
import storageUtils from "../utils/storage";
import { exportContactAsVCard } from "../utils/exportUtils";
import { showErrorAlert } from "../utils/errorHandler";

const mockTakePhoto = jest.fn();
const mockRequestCameraPermission = jest.fn();

jest.mock("@react-navigation/native", () => {
  const React = require("react");
  return {
    useFocusEffect: (callback: () => void | (() => void)) => {
      React.useEffect(() => {
        const cleanup = callback();
        return cleanup;
      }, [callback]);
    },
  };
});

jest.mock("react-native-vision-camera", () => {
  const React = require("react");
  const { View } = require("react-native");
  const MockCamera = React.forwardRef((_props: unknown, ref: unknown) => {
    React.useImperativeHandle(ref, () => ({
      takePhoto: mockTakePhoto,
    }));

    return <View testID="mock-camera" />;
  });

  Object.assign(MockCamera, {
    requestCameraPermission: mockRequestCameraPermission,
  });

  return {
    Camera: MockCamera,
    useCameraDevice: jest.fn(() => ({
      id: "back-camera",
      position: "back",
    })),
  };
});

jest.mock("rn-mlkit-ocr", () => ({
  __esModule: true,
  default: {
    recognizeText: jest.fn(),
  },
}));

jest.mock("../utils/storage", () => ({
  __esModule: true,
  default: {
    addContact: jest.fn(),
    getOcrLanguages: jest.fn(),
    getAutoSaveEnabled: jest.fn(),
  },
}));

jest.mock("../utils/exportUtils", () => ({
  __esModule: true,
  exportContactAsVCard: jest.fn(),
}));

jest.mock("../utils/errorHandler", () => ({
  __esModule: true,
  showErrorAlert: jest.fn(),
}));

const mockedMlkitOcr = MlkitOcr as jest.Mocked<typeof MlkitOcr>;
const mockedStorageUtils = storageUtils as jest.Mocked<typeof storageUtils>;
const mockedUseCameraDevice = useCameraDevice as jest.MockedFunction<
  typeof useCameraDevice
>;
const mockedExportContactAsVCard = exportContactAsVCard as jest.MockedFunction<
  typeof exportContactAsVCard
>;
const mockedShowErrorAlert = showErrorAlert as jest.MockedFunction<
  typeof showErrorAlert
>;
const mockedAlert = Alert.alert as jest.MockedFunction<typeof Alert.alert>;
let consoleWarnSpy: jest.SpyInstance;
let consoleErrorSpy: jest.SpyInstance;

describe("ScannerScreen", () => {
  beforeAll(() => {
    consoleErrorSpy = jest
      .spyOn(console, "error")
      .mockImplementation((...args) => {
        const [message] = args;

        if (
          typeof message === "string" &&
          message.includes("not wrapped in act")
        ) {
          return;
        }
      });
  });

  beforeEach(() => {
    jest.clearAllMocks();
    consoleWarnSpy = jest.spyOn(console, "warn").mockImplementation(() => {});

    const visionCameraModule = jest.requireMock(
      "react-native-vision-camera"
    ) as {
      Camera: { requestCameraPermission?: typeof mockRequestCameraPermission };
    };
    visionCameraModule.Camera.requestCameraPermission =
      mockRequestCameraPermission;

    mockedUseCameraDevice.mockReturnValue({
      id: "back-camera",
      position: "back",
    } as never);
    mockRequestCameraPermission.mockResolvedValue("granted");
    mockTakePhoto.mockResolvedValue({ path: "/tmp/test-photo.jpg" });
    mockedMlkitOcr.recognizeText.mockResolvedValue({
      text: "John Doe\njohn.doe@example.com\n+1-555-123-4567\nAcme Inc.",
      blocks: [],
    });
    mockedStorageUtils.getOcrLanguages.mockResolvedValue(["eng"]);
    mockedStorageUtils.getAutoSaveEnabled.mockResolvedValue(true);
    mockedStorageUtils.addContact.mockImplementation(
      async (contact) => contact
    );
    mockedExportContactAsVCard.mockResolvedValue();
  });

  afterEach(() => {
    consoleWarnSpy.mockRestore();
  });

  afterAll(() => {
    consoleErrorSpy.mockRestore();
  });

  it("shows the denied state when camera permission is rejected", async () => {
    mockRequestCameraPermission.mockResolvedValueOnce("denied");

    render(<ScannerScreen />);

    expect(
      await screen.findByText(/Camera permission is required to scan/i)
    ).toBeTruthy();
    expect(screen.getByTestId("grant-permission-button")).toBeTruthy();
  });

  it("captures text and auto-saves using the selected OCR detector", async () => {
    mockedStorageUtils.getOcrLanguages.mockResolvedValueOnce(["chi_sim"]);

    render(<ScannerScreen />);

    await waitFor(() => {
      expect(screen.getByTestId("capture-button")).toBeTruthy();
    });

    fireEvent.press(screen.getByTestId("capture-button"));

    expect(await screen.findByText("Saved to contacts")).toBeTruthy();
    expect(mockedMlkitOcr.recognizeText).toHaveBeenCalledWith(
      "file:///tmp/test-photo.jpg",
      "chinese"
    );
    expect(mockedStorageUtils.addContact).toHaveBeenCalledWith(
      expect.objectContaining({
        name: "John Doe",
        email: "john.doe@example.com",
        phone: "+1-555-123-4567",
        company: "Acme Inc.",
        id: expect.any(String),
        scannedAt: expect.any(String),
      })
    );
  });

  it("allows manual save when auto-save is disabled", async () => {
    mockedStorageUtils.getAutoSaveEnabled.mockResolvedValueOnce(false);

    render(<ScannerScreen />);

    await waitFor(() => {
      expect(screen.getByTestId("capture-button")).toBeTruthy();
    });

    fireEvent.press(screen.getByTestId("capture-button"));

    expect(await screen.findByText("Save Contact")).toBeTruthy();
    expect(mockedStorageUtils.addContact).not.toHaveBeenCalled();

    fireEvent.press(screen.getByTestId("save-contact-button"));

    await waitFor(() => {
      expect(mockedStorageUtils.addContact).toHaveBeenCalledTimes(1);
    });
    expect(mockedAlert).toHaveBeenCalledWith(
      "Success",
      "Contact saved successfully!"
    );
  });

  it("exports the parsed contact when export is pressed", async () => {
    mockedStorageUtils.getAutoSaveEnabled.mockResolvedValueOnce(false);

    render(<ScannerScreen />);

    await waitFor(() => {
      expect(screen.getByTestId("capture-button")).toBeTruthy();
    });

    fireEvent.press(screen.getByTestId("capture-button"));
    await screen.findByText("Export");

    fireEvent.press(screen.getByTestId("export-contact-button"));

    await waitFor(() => {
      expect(mockedExportContactAsVCard).toHaveBeenCalledWith(
        expect.objectContaining({
          name: "John Doe",
          email: "john.doe@example.com",
        })
      );
    });
  });

  it("shows an OCR error through the shared error handler", async () => {
    mockedMlkitOcr.recognizeText.mockRejectedValueOnce(new Error("OCR failed"));

    render(<ScannerScreen />);

    await waitFor(() => {
      expect(screen.getByTestId("capture-button")).toBeTruthy();
    });

    fireEvent.press(screen.getByTestId("capture-button"));

    await waitFor(() => {
      expect(mockedShowErrorAlert).toHaveBeenCalledWith(
        expect.objectContaining({ message: "OCR failed" }),
        "OCR processing"
      );
    });
  });
});
