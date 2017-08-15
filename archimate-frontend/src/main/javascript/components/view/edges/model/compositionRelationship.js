import React from 'react'
import _ from 'lodash'

import { StructuralRelationshipsWidget } from '../BaseLinkWidget'

export const TYPE='compositionRelationship';

export class CompositionRelationshipWidget extends StructuralRelationshipsWidget {
  getBaseClassName(link) { return TYPE; }
}

