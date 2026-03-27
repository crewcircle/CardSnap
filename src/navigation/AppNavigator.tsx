import React from "react";
import { createBottomTabNavigator } from "@react-navigation/bottom-tabs";
import { createNativeStackNavigator } from "@react-navigation/native-stack";
import MaterialCommunityIcons from "react-native-vector-icons/MaterialCommunityIcons";
import ScannerScreen from "../screens/ScannerScreen";
import ContactsScreen from "../screens/ContactsScreen";
import SettingsScreen from "../screens/SettingsScreen";
import EditContactScreen from "../screens/EditContactScreen";
import { ContactsStackParamList, RootTabParamList } from "./types";

const Tab = createBottomTabNavigator<RootTabParamList>();
const ContactsStack = createNativeStackNavigator<ContactsStackParamList>();

const ScanIcon = ({ color, size }: { color: string; size: number }) => (
  <MaterialCommunityIcons name="scan-helper" color={color} size={size} />
);

const ContactsIcon = ({ color, size }: { color: string; size: number }) => (
  <MaterialCommunityIcons name="account-multiple" color={color} size={size} />
);

const SettingsIcon = ({ color, size }: { color: string; size: number }) => (
  <MaterialCommunityIcons name="cog" color={color} size={size} />
);

const ContactsStackNavigator = () => {
  return (
    <ContactsStack.Navigator screenOptions={{ headerShown: false }}>
      <ContactsStack.Screen name="ContactsList" component={ContactsScreen} />
      <ContactsStack.Screen name="EditContact" component={EditContactScreen} />
    </ContactsStack.Navigator>
  );
};

export const AppNavigator = () => {
  return (
    <Tab.Navigator
      initialRouteName="Scan"
      screenOptions={{
        tabBarActiveTintColor: "#0066cc",
        tabBarInactiveTintColor: "#666666",
      }}
    >
      <Tab.Screen
        name="Scan"
        component={ScannerScreen}
        options={{
          tabBarLabel: "Scan",
          tabBarIcon: ScanIcon,
        }}
      />
      <Tab.Screen
        name="Contacts"
        component={ContactsStackNavigator}
        options={{
          tabBarLabel: "Contacts",
          tabBarIcon: ContactsIcon,
        }}
      />
      <Tab.Screen
        name="Settings"
        component={SettingsScreen}
        options={{
          tabBarLabel: "Settings",
          tabBarIcon: SettingsIcon,
        }}
      />
    </Tab.Navigator>
  );
};
