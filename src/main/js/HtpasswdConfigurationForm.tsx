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

import React from "react";
import { Button, Checkbox, InputField, Select } from "@scm-manager/ui-components";
import { withTranslation, WithTranslation } from "react-i18next";
import TestAuthenticationDialog from "./TestAuthenticationDialog";

type HtpasswdConfiguration = {
  htpasswdFilepath: string;
  htgroupFilepath: string;
  htmetaFilepath: string;
  enabled: boolean;
};

type Props = WithTranslation & {
  initialConfiguration: HtpasswdConfiguration;
  readOnly: boolean;
  onConfigurationChange: (config: HtpasswdConfiguration, valid: boolean) => void;
};

type State = HtpasswdConfiguration & {
  activeFields: string[];
  showTestDialog: boolean;
};

class HtpasswdConfigurationForm extends React.Component<Props, State> {
  constructor(props: Props) {
    super(props);
    this.state = {
      ...props.initialConfiguration,
      activeFields: [],
      showTestDialog: false
    };
  }

  valueChangeHandler = (value: any, name: string) => {
    this.setState(
      {
        [name]: value
      },
      () =>
        this.props.onConfigurationChange(
          {
            ...this.state
          },
          true
        )
    );
  };

  render(): React.ReactNode {
    const { t } = this.props;

    const testDialog = this.state.showTestDialog ? (
      <TestAuthenticationDialog
        config={this.state}
        testLink={this.props.initialConfiguration._links.test.href}
        onClose={() =>
          this.setState({
            showTestDialog: false
          })
        }
      />
    ) : null;

    return (
      <div className="columns is-multiline">
        {this.createInputField("htpasswdFilepath")}
        {this.createInputField("htgroupFilepath")}
        {this.createInputField("htmetaFilepath")}
        <div className="column is-full">
          {this.createCheckbox("enabled")}
        </div>
        <div className="column is-full">
          <Button
            label={t("scm-htpasswd-plugin.form.testButton")}
            disabled={!this.props.initialConfiguration._links.test}
            action={this.testAuthentication}
          />
        </div>
        {testDialog}
      </div>
    );
  }

  testAuthentication = () => {
    this.setState({
      showTestDialog: true
    });
  };

  createDropDown = (name: string, options: string[], handler = this.valueChangeHandler) => {
    const { t } = this.props;
    return this.ifActive(
      name,
      <div className="column is-half">
        <Select
          name={name}
          label={t("scm-htpasswd-plugin.form." + name)}
          helpText={t("scm-htpasswd-plugin.form." + name + "Help")}
          value={this.state[name]}
          options={this.createOptions(name, options)}
          onChange={handler}
        />
      </div>
    );
  };

  createOptions = (name: string, options: string[]) => {
    const { t } = this.props;
    return options.map(value => {
      return {
        value: value,
        label: t("scm-htpasswd-plugin.form.options." + name + "." + value)
      };
    });
  };

  createInputField = (name: string, type = "text", className: string = "is-half", disabled: boolean = false) => {
    const { t, readOnly } = this.props;
    return this.ifActive(
      name,
      <div className={`column ${className}`}>
        <InputField
          name={name}
          label={t("scm-htpasswd-plugin.form." + name)}
          helpText={t("scm-htpasswd-plugin.form." + name + "Help")}
          disabled={readOnly || disabled}
          value={this.state[name]}
          type={type}
          onChange={this.valueChangeHandler}
        />
      </div>
    );
  };

  createCheckbox = (name: string) => {
    const { t, readOnly } = this.props;
    return this.ifActive(
      name,
      <Checkbox
        name={name}
        label={t("scm-htpasswd-plugin.form." + name)}
        helpText={t("scm-htpasswd-plugin.form." + name + "Help")}
        checked={this.state[name]}
        disabled={readOnly}
        onChange={this.valueChangeHandler}
      />
    );
  };

  ifActive = (name: string, component: any) => {
    if (this.state.activeFields.includes(name)) {
      return null;
    } else {
      return component;
    }
  };
}

export default withTranslation("plugins")(HtpasswdConfigurationForm);
