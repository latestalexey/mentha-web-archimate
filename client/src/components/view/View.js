import React from 'react'
import { connect } from 'react-redux'

import Markers from './edges/markers'
import ViewDiagram from './ViewDiagram'
import Palette from './Palette'
import { LeftPanel, RightPanel } from "./Panels"

class View extends React.Component {

  componentWillMount() {
  }

  componentWillReceiveProps(nextProps) {
  }

  render() {
    const { id } = this.props
    const style = {
      display: 'flex',
      height: '100%'
    };
    return (
      <div className="view" style={style}>
        <Markers/>
        <LeftPanel>
          <Palette/>
        </LeftPanel>
        <ViewDiagram id={id}/>
        <RightPanel>
        </RightPanel>
      </div>
    )
  }
}

const mapStateToProps = (state, ownProps) => {
  return { id: ownProps.id };
};

const mapDispatchToProps = (dispatch) => ({
});


export default connect(mapStateToProps, mapDispatchToProps)(View)
