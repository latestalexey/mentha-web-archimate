import React from 'react'
import _ from 'lodash'

import { ModelNodeWidget } from '../BaseNodeWidget'

export const TYPE='workPackage';

export class WorkPackageWidget extends ModelNodeWidget {
  
  getClassName(node) { return 'a-node model_i workPackage'; }
}

