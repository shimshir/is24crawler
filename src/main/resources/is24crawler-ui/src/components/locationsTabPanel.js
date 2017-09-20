import React from 'react'
import {Component, Actions, Effect} from 'jumpsuit'
import {Nav, NavItem} from 'react-bootstrap'
import httpClient from '../httpClient'
import SuggestionInput from './suggestionInput'

Effect('fetchLocations', () => {
    httpClient.get('/api/locations').then(res => {
        Actions.setLocations(res.data);
    })
});

const LocationsTabPanel = Component(
    {
        componentWillMount() {
            this.setState({activeKey: "byPlace"});
            Actions.fetchLocations();
        },

        handleSelect(eventKey) {
            this.setState({activeKey: eventKey});
            Actions.setLocationSearchType(eventKey);
        },

        onChangePlaceTags(placeTags) {
            const byPlaceSearch = {
                type: "byPlace",
                geoNodes: placeTags.map(tag => tag.id)
            };
            Actions.setByPlaceSearch(byPlaceSearch)
        },

        defaultPlaceTags() {
            if (this.props.locations.length === 0) {
                return [{id: 1276003001, text: 'Berlin'}];
            } else {
                return this.props.byPlaceSearch.geoNodes.map(
                    geoNode => this.props.locations.find(location => location.key === geoNode)
                ).map(location => {
                    return {id: location.key, text: location.value}
                });
            }
        },

        render() {
            const suggestionData = this.props.locations.map(location => {
                return {id: location.key, text: location.value}
            });

            return (
                <div>
                    <Nav bsStyle="tabs" activeKey={this.state.activeKey} onSelect={this.handleSelect}>
                        <NavItem eventKey="byPlace">By Place</NavItem>
                        <NavItem eventKey="byTime">By Time</NavItem>
                        <NavItem eventKey="byDistance">By Distance</NavItem>
                    </Nav>
                    <br/>
                    {
                        this.state.activeKey === "byPlace" ? <SuggestionInput
                                                               id="locationsInput"
                                                               suggestions={suggestionData}
                                                               defaultTags={this.defaultPlaceTags()}
                                                               placeholder="Add Location"
                                                               onTagsChange={this.onChangePlaceTags}/>
                            : <h4>Not yet implemented</h4>
                    }
                </div>
            );
        }
    }, state => {
        return {
            locations: state.backendData.locations,
            byPlaceSearch: state.search.byPlaceSearch
        };
    }
);

export default LocationsTabPanel;