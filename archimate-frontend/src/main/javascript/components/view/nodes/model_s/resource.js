import React from 'react'
import _ from 'lodash'

import { ModelNodeWidget } from '../BaseNodeWidget'

export const TYPE='resource';

export class ResourceWidget extends ModelNodeWidget {
  
  getClassName(node) { return 'a-node model_s resource'; }
}

