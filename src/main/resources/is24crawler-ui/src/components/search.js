import React from 'react'
import {Component, State, Actions, Effect} from 'jumpsuit'
import {FormGroup, ControlLabel, FormControl, Button} from 'react-bootstrap'
import httpClient from '../httpClient'
import loadingImage from '../img/rotating-ring-loader.svg'
import SuggestionInput from './suggestionInput'

const isPositiveNumeric = (n) => {
    return !isNaN(parseFloat(n)) && isFinite(n) && parseFloat(n) >= 0;
};

const SearchState = State(
    {
        initial: {
            maxTotalPrice: '600',
            minRooms: '1.5',
            minSquare: '50',
            searchingStatus: 'NOT_SEARCHING',
            locationNodes: []
        },
        setMaxTotalPrice(state, maxTotalPrice) {
            return maxTotalPrice === '' || isPositiveNumeric(maxTotalPrice) ? {...state, maxTotalPrice} : state;
        },
        setMinRooms(state, minRooms) {
            return minRooms === '' || isPositiveNumeric(minRooms) ? {...state, minRooms} : state;
        },
        setMinSquare(state, minSquare) {
            return minSquare === '' || isPositiveNumeric(minSquare) ? {...state, minSquare} : state;
        },
        setLocationNodes(state, locationNodes) {
            return {...state, locationNodes};
        },
        setSearchingStatus(state, searchingStatus) {
            return {...state, searchingStatus};
        }
    }
);

Effect('submitSearch', (searchData) => {
    Actions.setSearchingStatus('SEARCHING');
    httpClient.post('/api/exposes', searchData).then(res => {
        Actions.setExposes(res.data);
        Actions.setSearchingStatus('RECEIVED_RESULTS');
    }).catch(error => {
        if (error.response && error.response.status === 503) {
            Actions.setSearchingStatus('ERROR_SERVICE_UNAVAILABLE');
        } else {
            Actions.setSearchingStatus('ERROR');
        }
    });
});

Effect('fetchLocations', () => {
    httpClient.get('/api/locations').then(res => {
        Actions.setLocations(res.data);
    })
});

const Search = Component(
    {
        submitForm() {
            Actions.submitSearch(
                {
                    maxTotalPrice: parseFloat(this.props.search.maxTotalPrice),
                    minRooms: parseFloat(this.props.search.minRooms),
                    minSquare: parseFloat(this.props.search.minSquare),
                    locationNodes: this.props.search.locationNodes
                }
            )
        },

        onChangeLocations(locationTags) {
            Actions.setLocationNodes(locationTags.map(tag => tag.id))
        },

        componentWillMount() {
            Actions.fetchLocations();
        },

        render() {
            const suggestionData = this.props.locations.map(location => {
                return {id: location.key, text: location.value}
            });
            return (
                <div>
                    <form onSubmit={e => e.preventDefault()}>
                        <FormGroup>
                            <ControlLabel htmlFor="maxTotalPriceInput">Max. total price</ControlLabel>
                            <FormControl
                                id="maxTotalPriceInput"
                                type="text"
                                value={this.props.search.maxTotalPrice}
                                placeholder="Enter price in €"
                                onChange={event => Actions.setMaxTotalPrice(event.target.value)}
                            />
                            <ControlLabel htmlFor="minRoomsInput">Min. rooms</ControlLabel>
                            <FormControl
                                id="minRoomsInput"
                                type="text"
                                value={this.props.search.minRooms}
                                placeholder="Enter the amount of rooms"
                                onChange={event => Actions.setMinRooms(event.target.value)}
                            />
                            <ControlLabel htmlFor="minSquareInput">Min. square meters</ControlLabel>
                            <FormControl
                                id="minSquareInput"
                                type="text"
                                value={this.props.search.minSquare}
                                placeholder="Enter the square meters"
                                onChange={event => Actions.setMinSquare(event.target.value)}
                            />
                            <ControlLabel htmlFor="locationsInput">Locations</ControlLabel>
                            <SuggestionInput
                                id="locationsInput"
                                suggestions={suggestionData}
                                defaultTags={[{id: 1276003001, text: "Berlin"}]}
                                placeholder="Add Location"
                                onTagsChange={this.onChangeLocations}/>
                        </FormGroup>
                        <Button
                            type="submit"
                            onClick={this.submitForm}>
                            Submit
                        </Button>
                        <span style={{margin: "0 5px"}}>
                            {this.props.search.searchingStatus === 'SEARCHING' ? <img src={loadingImage} alt="Searching for exposes"/> : null}
                            {this.props.search.searchingStatus === 'ERROR_SERVICE_UNAVAILABLE' ? <span>Search service did not respond in time</span> : null}
                            {this.props.search.searchingStatus === 'ERROR' ? <span>Unknown error occurred</span> : null}
                        </span>
                    </form>
                </div>
            )
        }
    }, (state) => {
        return {
            search: state.search,
            locations: state.backendData.locations
        };
    }
);

export {Search, SearchState};