import React from 'react'
import SuggestionTags from './suggestionTags'
import {Actions} from 'jumpsuit'
import {ControlLabel, FormGroup, FormControl, Row, Col} from 'react-bootstrap'

const ByDistanceTabContent = ({suggestionData, defaultByDistanceTags, onChangeByDistanceTags, onRadiusChange, currentRadius}) => {
    return (
        <Row>
            <Col sm={4}>
                <FormGroup>
                    <Row>
                        <Col componentClass={ControlLabel} sm={2}>
                            Place
                        </Col>
                        <Col sm={10}>
                            <SuggestionTags
                                key="byDistanceInput"
                                id="byDistanceInput"
                                suggestions={suggestionData}
                                maxTagsSize={1}
                                defaultTags={defaultByDistanceTags}
                                placeholder="Add Location"
                                onInputChange={value => Actions.fetchLocations(value)}
                                onTagsChange={onChangeByDistanceTags}/>
                        </Col>
                    </Row>
                    <br/>
                    <Row>
                        <Col componentClass={ControlLabel} sm={2}>
                            Radius
                        </Col>
                        <Col componentClass={ControlLabel} sm={10}>
                            <FormControl
                                componentClass="select"
                                placeholder="1"
                                onChange={e => onRadiusChange(e.target.value)}
                                defaultValue={currentRadius}
                            >
                                <option value={1}>1 Km</option>
                                <option value={5}>5 Km</option>
                                <option value={10}>10 Km</option>
                                <option value={15}>15 Km</option>
                                <option value={30}>30 Km</option>
                            </FormControl>
                        </Col>
                    </Row>
                </FormGroup>
            </Col>
        </Row>
    );
};

export default ByDistanceTabContent;
