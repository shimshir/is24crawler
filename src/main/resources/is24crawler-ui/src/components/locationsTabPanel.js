import React from 'react'
import {Component, Actions, Effect} from 'jumpsuit'
import {Nav, NavItem} from 'react-bootstrap'
import httpClient from '../httpClient'
import SuggestionTags from './suggestionTags'
import ByDistanceTabContent from './byDistanceTabContent'

Effect('fetchLocations', (labelQuery) => {
    httpClient.get(`/api/locations?label=${labelQuery}`).then(res => {
        Actions.setLocations(res.data);
    })
});

const LocationsTabPanel = Component(
    {
        componentWillMount() {
            this.setState({activeKey: "byPlace"});
        },

        handleSelect(eventKey) {
            this.setState({activeKey: eventKey});
            Actions.setLocationSearchType(eventKey);
        },

        onChangeByPlaceTags(byPlaceTags) {
            const byPlaceSearch = {
                type: "byPlace",
                selectedLocations: byPlaceTags.map(tag => {
                    return {id: tag.id, label: tag.text}
                })
            };
            Actions.setByPlaceSearch(byPlaceSearch)
        },

        onChangeByDistanceTags(byDistanceTags) {
            if (byDistanceTags.length !== 0) {
                const oneAndOnlyTag = byDistanceTags[0];
                const byDistanceSearch = {
                    type: "byDistance",
                    selectedLocation: {id: oneAndOnlyTag.id, label: oneAndOnlyTag.text}
                };
                Actions.setByDistanceSearch(byDistanceSearch)
            }
        },

        onRadiusChange(radius) {
            Actions.setByDistanceRadius(parseInt(radius, 10))
        },

        defaultByPlaceTags() {

            if (!this.props.byPlaceSearch.selectedLocations) {
                return [];
            } else {
                return this.props.byPlaceSearch.selectedLocations.map(location => {
                    return {id: location.id, text: location.label}
                })
            }
        },

        defaultByDistanceTags() {

            if (!this.props.byDistanceSearch.selectedLocation) {
                return [];
            } else {
                const loc = this.props.byDistanceSearch.selectedLocation;
                return [{id: loc.id, text: loc.label}];
            }
        },

        render() {
            const suggestionData = this.props.locations.map(location => {
                return {id: location.id, text: location.label}
            });

            return (
                <div>
                    <Nav bsStyle="tabs" activeKey={this.state.activeKey} onSelect={this.handleSelect}>
                        <NavItem eventKey="byPlace">By Place</NavItem>
                        <NavItem eventKey="byDistance">By Distance</NavItem>
                        <NavItem eventKey="byTime">By Time</NavItem>
                    </Nav>
                    <br/>
                    {
                        this.state.activeKey === "byPlace" ? <SuggestionTags
                                                               key="byPlaceInput"
                                                               id="byPlaceInput"
                                                               suggestions={suggestionData}
                                                               defaultTags={this.defaultByPlaceTags()}
                                                               placeholder="Add Location"
                                                               onInputChange={value => Actions.fetchLocations(value)}
                                                               onTagsChange={this.onChangeByPlaceTags}/>
                            : this.state.activeKey === "byDistance" ? <ByDistanceTabContent
                                                                        suggestionData={suggestionData}
                                                                        defaultByDistanceTags={this.defaultByDistanceTags()}
                                                                        onChangeByDistanceTags={this.onChangeByDistanceTags}
                                                                        onRadiusChange={this.onRadiusChange}
                                                                        currentRadius={this.props.byDistanceSearch.radius}
                                                                    />
                            : <h4>Not yet implemented</h4>
                    }
                </div>
            );
        }
    }, state => {
        return {
            locations: state.backendData.locations,
            byPlaceSearch: state.search.byPlaceSearch,
            byDistanceSearch: state.search.byDistanceSearch
        };
    }
);

export default LocationsTabPanel;