import React from 'react'
import _ from 'lodash'

import { ModelNodeWidget } from '../BaseNodeWidget'

export const TYPE='businessRole';

export class BusinessRoleWidget extends ModelNodeWidget {
  
  getClassName(node) { return 'a-node model_b businessRole'; }
}

