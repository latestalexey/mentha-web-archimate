import React from 'react'
import _ from 'lodash'

import { ModelNodeWidget } from '../BaseNodeWidget'

export const TYPE='applicationFunction';

export class ApplicationFunctionWidget extends ModelNodeWidget {
  
  getClassName(node) { return 'a-node model_a applicationFunction'; }
}

