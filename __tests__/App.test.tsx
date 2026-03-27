/**
 * @format
 */

import "react-native";
import React from "react";
import App from "../App";

import { it } from "@jest/globals";

import renderer from "react-test-renderer";

jest.mock("../src/navigation/AppNavigator", () => ({
  AppNavigator: () => "AppNavigator",
}));

it("renders correctly", () => {
  renderer.create(<App />);
});
