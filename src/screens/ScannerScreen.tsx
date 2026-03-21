import React, { useState, useRef, useCallback } from "react";
import {
  View,
  Text,
  TouchableOpacity,
  Image,
  StyleSheet,
  Alert,
  ActivityIndicator,
  Platform,
} from "react-native";
import { Camera } from "react-native-vision-camera";
import MlkitOcr from "rn-mlkit-ocr";
import MaterialCommunityIcons from "react-native-vector-icons/MaterialCommunityIcons";
import storageUtils from "../utils/storage";
import { exportContactAsVCard } from "../utils/exportUtils";
import { showErrorAlert } from "../utils/errorHandler";

// Types
type ContactInfo = {
  name?: string;
  email?: string;
  phone?: string;
  company?: string;
  address?: string;
  website?: string;
};

const ScannerScreen = () => {
  const cameraRef = useRef<any>(null);
  const [isProcessing, setIsProcessing] = useState(false);
  const [capturedImage, setCapturedImage] = useState<string | null>(null);
  const [extractedText, setExtractedText] = useState<string>("");
  const [contactInfo, setContactInfo] = useState<ContactInfo>({});
  const [showResults, setShowResults] = useState(false);
  const [permissionStatus, setPermissionStatus] = useState<
    "undetermined" | "denied" | "authorized" | "granted"
  >("undetermined");

  // Request camera permission
  const requestCameraPermission = useCallback(async () => {
    try {
      const status = await Camera.requestCameraPermission();
      // CameraPermissionRequestResult type can be 'undetermined', 'denied', or 'authorized'
      // but on Android it might return 'granted' instead of 'authorized'
      setPermissionStatus(status);
      // Explicitly check for both possible values to satisfy TypeScript
      // This comparison might appear unintentional to TypeScript but is necessary
      // to handle both iOS ("authorized") and Android ("granted") permission results
      const isAuthorized =
        (status as unknown as string) === "authorized" ||
        (status as unknown as string) === "granted";
      return isAuthorized;
    } catch (error) {
      console.warn("Camera permission error:", error);
      setPermissionStatus("denied");
      return false;
    }
  }, []);

  // Handle capturing image
  const handleCapture = useCallback(async () => {
    if (!cameraRef.current) return;

    try {
      setIsProcessing(true);
      const image = await cameraRef.current.takePicture();
      setCapturedImage(image.uri);

      // Process the image with OCR
      const result = await MlkitOcr.recognizeText(image.uri);
      const text = result.text;

      setExtractedText(text);
      const parsedInfo = parseContactInfo(text);
      setContactInfo(parsedInfo);
      setShowResults(true);
    } catch (error) {
      console.warn("Capture/OCR Error:", error);
      showErrorAlert(error, "OCR processing");
    } finally {
      setIsProcessing(false);
    }
  }, [cameraRef]);

  // Parse extracted text to find contact information
  const parseContactInfo = (text: string): ContactInfo => {
    const info: ContactInfo = {};

    // Simple regex patterns for common contact info
    const emailMatch = text.match(
      /[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\.[a-zA-Z]{2,}/
    );
    if (emailMatch) info.email = emailMatch[0];

    const phoneMatch = text.match(
      /(?:\+?\d{1,3}[-.\s]?)?\(?\d{3}\)?[-.\s]?\d{3}[-.\s]?\d{4}/
    );
    if (phoneMatch) info.phone = phoneMatch[0];

    // Try to find company name (look for common suffixes)
    const companyMatch = text.match(
      /(?:Inc|LLC|Ltd|Corp|Corporation|Company|Co\.)/i
    );
    if (companyMatch) {
      // Extract a line containing the company match
      const lines = text.split("\n");
      const companyLine = lines.find((line) => line.includes(companyMatch[0]));
      if (companyLine) info.company = companyLine.trim();
    }

    // Assume the first line might be a name (if it's short and doesn't contain @ or numbers)
    const lines = text.split("\n").filter((line) => line.trim() !== "");
    if (lines.length > 0) {
      const firstLine = lines[0].trim();
      if (
        firstLine.length < 50 &&
        !firstLine.includes("@") &&
        !/\d{3}/.test(firstLine)
      ) {
        info.name = firstLine;
      }
    }

    // Look for website
    const websiteMatch = text.match(
      /https?:\/\/(www\.)?[-a-zA-Z0-9@:%._\+~#=]{1,256}\.[a-zA-Z0-9()]{1,6}\b([-a-zA-Z0-9()@:%_\+.~#?&//=]*)/
    );
    if (websiteMatch) info.website = websiteMatch[0];

    return info;
  };

  // Handle saving contact
  const handleSaveContact = async () => {
    if (contactInfo.name || contactInfo.email || contactInfo.phone) {
      const contact = {
        name: contactInfo.name || "",
        email: contactInfo.email || "",
        phone: contactInfo.phone || "",
        company: contactInfo.company || "",
        address: contactInfo.address || "",
        website: contactInfo.website || "",
      };

      await storageUtils.addContact(contact);
      Alert.alert("Success", "Contact saved successfully!");
      setShowResults(false);
      setExtractedText("");
      setContactInfo({});
      setCapturedImage(null);
    } else {
      Alert.alert("Error", "No contact information to save.");
    }
  };

  // Handle exporting contact as VCard
  const handleExportContact = async () => {
    if (contactInfo.name || contactInfo.email || contactInfo.phone) {
      const contact = {
        name: contactInfo.name || "",
        email: contactInfo.email || "",
        phone: contactInfo.phone || "",
        company: contactInfo.company || "",
        address: contactInfo.address || "",
        website: contactInfo.website || "",
      };

      try {
        await exportContactAsVCard(contact);
        Alert.alert("Success", "Contact exported as VCard!");
      } catch (error) {
        console.warn("Export error:", error);
        Alert.alert("Error", "Failed to export contact.");
      }
    } else {
      Alert.alert("Error", "No contact information to export.");
    }
  };

  // Retake photo
  const handleRetake = () => {
    setShowResults(false);
    setExtractedText("");
    setContactInfo({});
    setCapturedImage(null);
  };

  if (permissionStatus === "undetermined") {
    return (
      <View style={Styles.container}>
        <Text style={Styles.permissionText}>
          Requesting camera permission...
        </Text>
      </View>
    );
  }

  if (permissionStatus === "denied") {
    return (
      <View style={Styles.container}>
        <Text style={Styles.permissionText}>
          Camera permission is required to scan business cards.
        </Text>
        <TouchableOpacity
          style={Styles.button}
          onPress={requestCameraPermission}
        >
          <Text style={Styles.buttonText}>Grant Permission</Text>
        </TouchableOpacity>
      </View>
    );
  }

  return (
    <View style={Styles.container}>
      {!showResults ? (
        // Camera view
        <View style={{ flex: 1 }}>
          {/* @ts-ignore: Property 'device' is missing in type '{ ref: MutableRefObject<any>; style: AbsoluteFillStyle; isActive: true; }' but required in type 'Readonly<CameraProps>.' */}
          <Camera
            ref={cameraRef}
            style={StyleSheet.absoluteFillObject}
            isActive={true}
          />
          <View style={Styles.overlay}>
            <MaterialCommunityIcons name="scan-helper" size={40} color="#fff" />
            <Text style={Styles.instructionText}>
              Point camera at business card and tap to capture
            </Text>
          </View>
          <TouchableOpacity
            style={Styles.captureButton}
            onPress={handleCapture}
            disabled={isProcessing}
          >
            {isProcessing ? (
              <ActivityIndicator size="small" color="#fff" />
            ) : (
              <MaterialCommunityIcons name="camera" size={24} color="#fff" />
            )}
          </TouchableOpacity>
        </View>
      ) : (
        // Results view
        <View style={Styles.resultsContainer}>
          {capturedImage !== null ? (
            <Image
              source={{ uri: capturedImage }}
              style={Styles.capturedImage}
            />
          ) : null}

          <Text style={Styles.resultsTitle}>Extracted Information</Text>
          <Text style={Styles.resultsText}>{extractedText}</Text>

          <View style={Styles.contactInfoContainer}>
            <Text style={Styles.contactInfoLabel}>Name:</Text>
            <Text style={Styles.contactInfoValue}>
              {contactInfo.name || "Not detected"}
            </Text>

            <Text style={Styles.contactInfoLabel}>Email:</Text>
            <Text style={Styles.contactInfoValue}>
              {contactInfo.email || "Not detected"}
            </Text>

            <Text style={Styles.contactInfoLabel}>Phone:</Text>
            <Text style={Styles.contactInfoValue}>
              {contactInfo.phone || "Not detected"}
            </Text>

            <Text style={Styles.contactInfoLabel}>Company:</Text>
            <Text style={Styles.contactInfoValue}>
              {contactInfo.company || "Not detected"}
            </Text>

            <Text style={Styles.contactInfoLabel}>Website:</Text>
            <Text style={Styles.contactInfoValue}>
              {contactInfo.website || "Not detected"}
            </Text>
          </View>

          <View style={Styles.buttonContainer}>
            <TouchableOpacity style={Styles.button} onPress={handleRetake}>
              <MaterialCommunityIcons name="repeat" size={20} color="#fff" />
              <Text style={Styles.buttonText}>Retake</Text>
            </TouchableOpacity>

            <TouchableOpacity style={Styles.button} onPress={handleSaveContact}>
              <MaterialCommunityIcons
                name="content-save"
                size={20}
                color="#fff"
              />
              <Text style={Styles.buttonText}>Save Contact</Text>
            </TouchableOpacity>

            <TouchableOpacity
              style={Styles.button}
              onPress={handleExportContact}
            >
              <MaterialCommunityIcons
                name="share-variant"
                size={20}
                color="#fff"
              />
              <Text style={Styles.buttonText}>Export</Text>
            </TouchableOpacity>
          </View>
        </View>
      )}
    </View>
  );
};

// Styles must be declared after the component
const Styles = StyleSheet.create({
  container: {
    flex: 1,
    backgroundColor: "#000",
  },
  overlay: {
    position: "absolute",
    bottom: 20,
    left: 0,
    right: 0,
    alignItems: "center",
  },
  instructionText: {
    color: "#fff",
    fontSize: 16,
    marginTop: 8,
  },
  captureButton: {
    position: "absolute",
    bottom: 20,
    width: 60,
    height: 60,
    borderRadius: 30,
    backgroundColor: "#fff",
    alignItems: "center",
    justifyContent: "center",
    alignSelf: "center",
  },
  permissionText: {
    textAlign: "center",
    marginTop: 40,
    color: "#fff",
    fontSize: 18,
  },
  button: {
    marginVertical: 20,
    paddingHorizontal: 30,
    paddingVertical: 15,
    backgroundColor: "#0066cc",
    borderRadius: 25,
  },
  buttonText: {
    color: "#fff",
    fontSize: 16,
    fontWeight: "600",
  },
  resultsContainer: {
    flex: 1,
    backgroundColor: "#fff",
    padding: 20,
  },
  capturedImage: {
    width: "100%",
    height: 300,
    marginBottom: 20,
  },
  resultsTitle: {
    fontSize: 20,
    fontWeight: "600",
    marginBottom: 10,
    color: "#333",
  },
  resultsText: {
    fontSize: 14,
    color: "#666",
    marginBottom: 20,
    padding: 10,
    backgroundColor: "#f5f5f5",
    borderRadius: 8,
  },
  contactInfoContainer: {
    marginBottom: 20,
  },
  contactInfoLabel: {
    fontSize: 16,
    fontWeight: "600",
    color: "#333",
    marginBottom: 4,
  },
  contactInfoValue: {
    fontSize: 16,
    color: "#666",
    marginBottom: 12,
  },
  buttonContainer: {
    flexDirection: "row",
    justifyContent: "space-around",
  },
});

export default ScannerScreen;
