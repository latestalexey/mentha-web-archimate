import _ from 'lodash';
import { MODEL_NOOP_RECEIVED, MODEL_OBJECT_RECEIVED, MODEL_COMMIT_RECEIVED, MODEL_ERROR_RECEIVED } from "../actions"

const postProcessModel = (model) => ({
  ...model,
  views: _.mapValues(model.views, (view) => ({
    ...view,
    nodes: _.mapValues(view.nodes, (node) => {
      if (!!node.concept) { return { ...node, conceptInfo: model.nodes[node.concept] }; }
      return node;
    }),
    edges: _.mapValues(view.edges, (edge) => {
      if (!!edge.concept) { return {...edge, conceptInfo: model.edges[edge.concept]}; }
      return edge;
    })
  }))
});

const applyNoop = (model, payload) => {
    return model;
};

const applyObject = (model, payload) => {
  const apply = (model, obj) => {
    const tp = obj['_tp'].toLowerCase();
    if (tp === 'model') {
      return obj;
    }
    // TODO: apply other types
    return model;
  };

  const ids = Object.getOwnPropertyNames(payload);
  for (const id of ids) {
    const obj = payload[id];
    model = apply(model, obj);
  }

  return postProcessModel(model);
};

const applyCommit = (model, payload) => {

  const apply = (model, obj) => {
    model = { ...model };

    for (const name of Object.getOwnPropertyNames(obj)) {
      const prefix = name.substring(0, 1);
      const postfix = name.substring(1);
      const value = obj[name];
      switch (prefix) {
        case "=":
        case "+": {
          model[postfix] = value;
          break;
        }
        case "-": {
          delete model[postfix];
          break;
        }
        case "@": {
          model[postfix] = apply(model[postfix] || {}, value);
          break;
        }
      }
    }

    return model;
  };

  model = apply(model, payload);
  return postProcessModel(model);
};

const applyError = (model, payload) => {
    return model;
};


const getInitialState = () => ({
  nodes: {},
  edges: {},
  view: {}
});

const reducer = (state = getInitialState(), action) => {
  switch (action.type) {
    case MODEL_NOOP_RECEIVED: return applyNoop(state, action.payload);
    case MODEL_OBJECT_RECEIVED: return applyObject(state, action.payload);
    case MODEL_COMMIT_RECEIVED: return applyCommit(state, action.payload);
    case MODEL_ERROR_RECEIVED: return applyError(state, action.payload);
  }
  return state;
};

export default reducer