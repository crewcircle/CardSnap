// Jest setup file for consistent native module mocking

const React = require("react");

// Mock react-native with all basic components
jest.mock("react-native", () => {
  const createMockComponent = (name) => {
    return React.forwardRef(({ children, ...props }, ref) => {
      return React.createElement(name, { ...props, ref }, children);
    });
  };

  const FlatList = React.forwardRef(({
    data,
    renderItem,
    keyExtractor,
    ItemSeparatorComponent,
    ListEmptyComponent,
    ListHeaderComponent,
    ...props
  }, ref) => {
    const elements = [];

    if (ListHeaderComponent) {
      elements.push(
        React.createElement(
          React.Fragment,
          { key: "__header" },
          typeof ListHeaderComponent === "function"
            ? React.createElement(ListHeaderComponent)
            : ListHeaderComponent
        )
      );
    }

    if (!data || data.length === 0) {
      if (ListEmptyComponent) {
        elements.push(
          React.createElement(
            React.Fragment,
            { key: "__empty" },
            typeof ListEmptyComponent === "function"
              ? React.createElement(ListEmptyComponent)
              : ListEmptyComponent
          )
        );
      }
    } else {
      data.forEach((item, index) => {
        if (index > 0 && ItemSeparatorComponent) {
          elements.push(
            React.createElement(
              React.Fragment,
              { key: `__sep_${index}` },
              React.createElement(ItemSeparatorComponent)
            )
          );
        }
        elements.push(
          React.createElement(
            React.Fragment,
            { key: keyExtractor ? keyExtractor(item, index) : index },
            renderItem({ item, index })
          )
        );
      });
    }

    return React.createElement("FlatList", { ...props, ref }, elements);
  });

  return {
    React,
    View: createMockComponent("View"),
    Text: createMockComponent("Text"),
    TextInput: createMockComponent("TextInput"),
    TouchableOpacity: createMockComponent("TouchableOpacity"),
    TouchableHighlight: createMockComponent("TouchableHighlight"),
    ScrollView: createMockComponent("ScrollView"),
    KeyboardAvoidingView: createMockComponent("KeyboardAvoidingView"),
    FlatList,
    Switch: createMockComponent("Switch"),
    ActivityIndicator: createMockComponent("ActivityIndicator"),
    Image: createMockComponent("Image"),
    Modal: createMockComponent("Modal"),
    SafeAreaView: createMockComponent("SafeAreaView"),
    StatusBar: createMockComponent("StatusBar"),
    Pressable: createMockComponent("Pressable"),
    RefreshControl: createMockComponent("RefreshControl"),
    Platform: {
      OS: "ios",
      select: (obj) => obj.ios || obj.default,
    },
    StyleSheet: {
      create: (styles) => styles,
      flatten: (style) => {
        if (Array.isArray(style)) {
          return Object.assign({}, ...style);
        }
        return style || {};
      },
    },
    Alert: {
      alert: jest.fn(),
      prompt: jest.fn(),
    },
    Linking: {
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      openURL: jest.fn().mockResolvedValue(true),
      canOpenURL: jest.fn().mockResolvedValue(true),
      getInitialURL: jest.fn().mockResolvedValue("/"),
    },
    AppState: {
      addEventListener: jest.fn(),
      removeEventListener: jest.fn(),
      currentState: "active",
    },
    NativeModules: {
      LaunchArgs: {
        getConstants: () => ({ launchArgs: {} }),
        getLaunchArgs: jest.fn().mockResolvedValue({}),
      },
    },
  };
});

// Mock @react-navigation/native
jest.mock("@react-navigation/native", () => {
  return {
    NavigationContainer: ({ children }) => children,
    useNavigation: () => ({
      navigate: jest.fn(),
      goBack: jest.fn(),
    }),
    useFocusEffect: (callback) => {
      React.useEffect(() => {
        callback();
      }, [callback]);
    },
    createNativeStackNavigator: () => ({
      Navigator: ({ children }) => children,
      Screen: ({ children }) => children,
    }),
  };
});

// Mock @react-navigation/bottom-tabs
jest.mock("@react-navigation/bottom-tabs", () => ({
  createBottomTabNavigator: () => ({
    Navigator: ({ children }) => children,
    Screen: ({ children }) => children,
  }),
}));

// Mock react-native-vision-camera
jest.mock("react-native-vision-camera", () => ({
  Camera: React.forwardRef((props, ref) => {
    React.useImperativeHandle(ref, () => ({
      takePhoto: jest.fn().mockResolvedValue({ path: "/tmp/test-photo.jpg" }),
    }));
    return React.createElement("View", { testID: "mock-camera" });
  }),
  useCameraDevice: jest.fn(() => ({
    id: "back-camera",
    position: "back",
  })),
  useCameraDevices: jest.fn(() => ({
    back: { id: "back-camera", position: "back" },
    front: { id: "front-camera", position: "front" },
  })),
  requestCameraPermission: jest.fn().mockResolvedValue("granted"),
  PermissionStatus: {
    UNDETERMINED: "undetermined",
    DENIED: "denied",
    AUTHORIZED: "authorized",
  },
}));

// Mock rn-mlkit-ocr
jest.mock("rn-mlkit-ocr", () => ({
  __esModule: true,
  default: {
    recognizeText: jest.fn().mockResolvedValue({
      text: "John Doe\njohn.doe@example.com\n+1-555-123-4567\nAcme Inc.",
      blocks: [],
    }),
  },
}));

// Mock react-native-share
jest.mock("react-native-share", () => ({
  open: jest.fn().mockResolvedValue(true),
  isAvailable: jest.fn().mockResolvedValue(true),
}));

// Mock react-native-fs
jest.mock("react-native-fs", () => ({
  exists: jest.fn().mockResolvedValue(true),
  mkdir: jest.fn().mockResolvedValue(undefined),
  writeFile: jest.fn().mockResolvedValue(undefined),
  readFile: jest.fn().mockResolvedValue("test content"),
  unlink: jest.fn().mockResolvedValue(undefined),
  getFSInfo: jest.fn().mockResolvedValue({}),
  getAllExternalFilesDirs: jest.fn().mockResolvedValue([]),
  getExternalStorageDirectory: jest.fn().mockResolvedValue("/storage/emulated/0"),
  getPictureURL: jest.fn().mockResolvedValue("file:///test.jpg"),
  moveFile: jest.fn().mockResolvedValue(undefined),
  copyFile: jest.fn().mockResolvedValue(undefined),
  downloadFile: jest.fn().mockResolvedValue({ jobId: "123" }),
  stopDownload: jest.fn().mockResolvedValue(undefined),
}));

// Mock @react-native-async-storage/async-storage
jest.mock("@react-native-async-storage/async-storage", () => ({
  getItem: jest.fn(),
  setItem: jest.fn(),
  removeItem: jest.fn(),
  mergeItem: jest.fn(),
  clear: jest.fn(),
  getAllKeys: jest.fn(),
  flushGetRequests: jest.fn(),
  multiGet: jest.fn(),
  multiSet: jest.fn(),
  multiRemove: jest.fn(),
  multiMerge: jest.fn(),
}));

// Mock react-native-vector-icons/MaterialCommunityIcons
jest.mock("react-native-vector-icons/MaterialCommunityIcons", () => "Icon");
