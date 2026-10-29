/*
 * Copyright (c) 2020 - present Cloudogu GmbH
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, version 3.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program. If not, see https://www.gnu.org/licenses/.
 */

import React, { FC, useState } from "react";
import { useTranslation } from "react-i18next";
import { apiClient, InputField, Modal, Tag } from "@scm-manager/ui-components";
import { Button, Subtitle } from "@scm-manager/ui-core";

type TestResultUser = {
  valid: boolean;
  name: string;
  displayName: string;
  mailAddress?: string;
};

type TestResult = {
  configured?: boolean;
  userFound?: boolean;
  userAuthenticated?: boolean;
  exception?: string;
  user?: TestResultUser;
  groups?: string[];
};

type Props = {
  config: any;
  testLink: string;
  onClose: () => void;
};

const TestConnectionDialog: FC<Props> = ({ config, testLink, onClose }) => {
  const { t } = useTranslation("plugins");
  const [username, setUsername] = useState("");
  const [password, setPassword] = useState("");
  const [testResult, setTestResult] = useState<TestResult | undefined>(undefined);

  const onTest = () => {
    apiClient
      .post(testLink, {
        username,
        password,
        config,
      })
      .then((result) => result.json())
      .then((body) => setTestResult(body))
      .catch((error) =>
        setTestResult({
          exception: error.message || "Unknown error",
        }),
      );
  };

  const renderTestResult = () => {
    if (!testResult) {
      return null;
    }

    const success = <Tag color="success" label="Success" />;
    const failure = <Tag color="danger" label="Failure" />;
    const successOrFailure = (r?: boolean) => (r ? success : failure);

    return (
      <>
        <hr />
        <Subtitle subtitle={t("scm-htpasswd-plugin.testForm.result.header")} />
        <table className="table">
          <tbody>
            <tr>
              <td>{t("scm-htpasswd-plugin.testForm.result.configured")}</td>
              <td>{successOrFailure(testResult.configured)}</td>
            </tr>
            <tr>
              <td>{t("scm-htpasswd-plugin.testForm.result.userFound")}</td>
              <td>{successOrFailure(testResult.userFound)}</td>
            </tr>
            <tr>
              <td>{t("scm-htpasswd-plugin.testForm.result.userAuthenticated")}</td>
              <td>{successOrFailure(testResult.userAuthenticated)}</td>
            </tr>
            {testResult.user ? (
              <>
                <tr>
                  <td>{t("scm-htpasswd-plugin.testForm.result.userValid")}</td>
                  <td>{successOrFailure(testResult.user.valid)}</td>
                </tr>
                <tr>
                  <td>{t("scm-htpasswd-plugin.testForm.result.userDetails")}</td>
                  <td>
                    <ul>
                      <li>
                        {t("scm-htpasswd-plugin.testForm.result.userDetailsName")}: {testResult.user.name}
                      </li>
                      <li>
                        {t("scm-htpasswd-plugin.testForm.result.userDetailsDisplayName")}: {testResult.user.displayName}
                      </li>
                      <li>
                        {t("scm-htpasswd-plugin.testForm.result.userDetailsMail")}:{" "}
                        {testResult.user.mailAddress || t("scm-htpasswd-plugin.testForm.result.missingValidMail")}
                      </li>
                    </ul>
                  </td>
                </tr>
                <tr>
                  <td>{t("scm-htpasswd-plugin.testForm.result.groups")}</td>
                  <td>{testResult.groups?.join(", ")}</td>
                </tr>
              </>
            ) : (
              <tr>
                <td>{t("scm-htpasswd-plugin.testForm.result.exception")}</td>
                <td>{testResult.exception}</td>
              </tr>
            )}
          </tbody>
        </table>
      </>
    );
  };

  const valid = username !== "" && password !== "";

  const body = (
    <>
      <div className="columns">
        <div className="column">
          <InputField
            name="username"
            label={t("scm-htpasswd-plugin.testForm.username")}
            value={username}
            onReturnPressed={onTest}
            onChange={setUsername}
          />
        </div>
        <div className="column">
          <InputField
            name="password"
            label={t("scm-htpasswd-plugin.testForm.password")}
            value={password}
            type="password"
            onReturnPressed={onTest}
            onChange={setPassword}
          />
        </div>
      </div>
      {renderTestResult()}
    </>
  );

  const footer = (
    <>
      <Button disabled={!valid} onClick={onTest} variant="primary">{t("scm-htpasswd-plugin.testForm.submit")}</Button>
      <Button onClick={onClose}>{t("scm-htpasswd-plugin.testForm.abort")}</Button>
    </>
  );

  return (
    <Modal
      title={t("scm-htpasswd-plugin.testForm.title")}
      closeFunction={onClose}
      body={body}
      footer={footer}
      active={true}
    />
  );
};

export default TestConnectionDialog;
