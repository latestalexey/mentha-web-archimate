import React from 'react'
import _ from 'lodash'

import { ModelNodeWidget } from '../BaseNodeWidget'

export const TYPE='facility';

export class FacilityWidget extends ModelNodeWidget {
  constructor(props) { super(props); }
  getClassName(node) { return 'a-node model_p facility'; }
}

