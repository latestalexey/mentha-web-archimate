import React from 'react'
import _ from 'lodash'

import { ModelLinkWidget } from '../BaseLinkWidget'

export const TYPE='specializationRelationship';

export class SpecializationRelationshipWidget extends ModelLinkWidget {
  getBaseClassName(link) { return TYPE; }
}

