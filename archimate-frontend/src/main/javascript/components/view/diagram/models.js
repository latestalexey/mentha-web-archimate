import _ from 'lodash';

export const generateId = () => (
  'xxxxxxxx-xxxx-4xxx-yxxx-xxxxxxxxxxxx'.replace(/[xy]/g, c => {
    const r = Math.random() * 16 | 0, v = c === 'x' ? r : (r & 0x3 | 0x8);
    return v.toString(16);
  })
);

class BaseEntity {
  constructor() {
  }
}

class BaseModel extends BaseEntity {

  static SELECTED_MODE_OFF = false;
  static SELECTED_MODE_SELECTED = true;
  static SELECTED_MODE_SELECTED_FOR_EDIT = 2;

  constructor() {
    super();
    this.selected = BaseModel.SELECTED_MODE_OFF;
  }

  isSelected() {
    return !!this.selected;
  }

  setSelected(selected) {
    this.selected = !!selected;
  }

  selectForEdit() {
    this.selected = BaseModel.SELECTED_MODE_SELECTED_FOR_EDIT;
  }

  isSelectedForEdit() {
    return (BaseModel.SELECTED_MODE_SELECTED_FOR_EDIT === this.selected);
  }

}

export class PointModel extends BaseModel {

  constructor(link, x, y) {
    super();
    this.x = x;
    this.y = y;
    this.link = link;
  }

  updateLocation(point) {
    this.x = point.x;
    this.y = point.y;
  }

}

export class LinkModel extends BaseModel {
  constructor(id, linkType = 'default') {
    super();
    this.id = id;
    this.linkType = linkType;
    this.sourceNode = null;
    this.targetNode = null;
    this.points = [ new PointModel(this, 0, 0), new PointModel(this, 0, 0) ];
  }

  setSourceNode(node) {
    if (this.sourceNode !== node) {
      if (!!this.sourceNode) {
        this.sourceNode.unregisterLink(this);
      }
      if (!!(this.sourceNode = node)) {
        node.registerLink(this);
        this.getFirstPoint().updateLocation(node);
      }
    }
  }

  setTargetNode(node) {
    if (this.targetNode !== node) {
      if (!!this.targetNode) {
        this.targetNode.unregisterLink(this);
      }
      if (!!(this.targetNode = node)) {
        node.registerLink(this);
        this.getLastPoint().updateLocation(node);
      }
    }
  }

  setMiddlePoints(points) {
    this.points = [
      this.getFirstPoint(),
      ... _.map(points, point => new PointModel(this, point.x, point.y)),
      this.getLastPoint()
    ];
  }

  getMiddlePoints() {
    return _.slice(this.points, 1, this.points.length-1);
  }

  getFirstPoint() {
    return _.first(this.points);
  }

  getLastPoint() {
    return _.last(this.points);
  }

  getPoint(index) {
    const l = this.points.length;
    const i = (l + index) % l;
    return this.points[i];
  }

  getPoints() {
    return this.points;
  }

  addPoint(point, index = 1) {
    const pointModel = new PointModel(this, point.x, point.y);
    this.points.splice(index, 0, pointModel);
    return pointModel;
  }

  removePoint(point) {
    this.points = _.filter(this.points, (p) => point !== p);
  }

  getType() {
    return this.linkType;
  }
}

export class PortModel extends BaseModel {
  constructor(node) {
    super();
    this.parentNode = node;
  }
}

export class NodeModel extends BaseModel {
  constructor(id, nodeType = 'default') {
    super();
    this.id = id;
    this.nodeType = nodeType;
    this.x = 0;
    this.y = 0;
    this.width = 100;
    this.height = 40;
    this.zIndex = 0;
    this.links = [];
  }

  registerLink(link) {
    this.links.push(link);
  }

  unregisterLink(link) {
    this.links = _.filter(this.links, (l) => l !== link);
  }

  getLinks() {
    return this.links;
  }

  getType() {
    return this.nodeType;
  }

  asPort() {
    return new PortModel(this);
  }
}

export class DiagramModel extends BaseEntity {
  constructor(id) {
    super();
    this.id = id;
    this.links = {};
    this.nodes = {};
    this.rendered = false;
  }

  getNode(node) {
    if (node instanceof NodeModel) {
      return node;
    }
    return this.nodes[node];
  }

  getLink(link) {
    if (link instanceof LinkModel) {
      return link;
    }
    return this.links[link];
  }

  addLink(link) {
    this.links[link.id] = link;
    return link;
  }

  addNode(node) {
    this.nodes[node.id] = node;
    return node;
  }

  getLinks() {
    return this.links;
  }

  getNodes() {
    return this.nodes;
  }

  setSelection(predicate) {
    _.forEach(this.getNodes(), (node) => {
      node.setSelected(predicate(node));
    });
    _.forEach(this.getLinks(), (link) => {
      let allSelected = predicate(link);
      _.forEach(link.getPoints(), (point) => {
        point.setSelected(predicate(point));
        allSelected |= point.isSelected();
      });
      link.setSelected(allSelected);
    });
  }

  getSelectedItems() {
    const result = [];
    _.forEach(this.getNodes(), (ref) => {
      if (ref.isSelected()) { result.push(ref); }
    });
    _.forEach(this.getLinks(), (ref) => {
      _.forEach(ref.getPoints(), (point) => {
        if (point.isSelected()) { result.push(point); }
      });
      if (ref.isSelected()) { result.push(ref); }
    });
    return result;
  }

  getRepaintEntities(entities) {
    const result = {};
    const addLink = (link) => {
      result[link.id] = true;
      if (link.sourceNode) { result[link.sourceNode.id] = true; }
      if (link.targetNode) { result[link.targetNode.id] = true; }
    };
    entities.forEach(entity => {
      result[entity.id] = true;
      if (entity instanceof NodeModel) {
        _.forEach(entity.getLinks(), link => addLink(link));
      } else if (entity instanceof PointModel) {
        addLink(entity.link);
      }
    });
    return result;
  }

}


