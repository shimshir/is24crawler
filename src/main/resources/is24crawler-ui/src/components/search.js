import React from 'react'
import {Component, State, Actions, Effect} from 'jumpsuit'
import {FormGroup, ControlLabel, FormControl, Button} from 'react-bootstrap'
import httpClient from '../httpClient'
import loadingImage from '../img/rotating-ring-loader.svg'
import LocationsTabPanel from './locationsTabPanel'

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
            byPlaceSearch: {},
            byDistanceSearch: {radius: 1},
            locationSearchType: 'byPlace'
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
        setLocationSearchType(state, locationSearchType) {
            return {...state, locationSearchType};
        },
        setByPlaceSearch(state, byPlaceSearch) {
            return {...state, byPlaceSearch};
        },
        setByDistanceSearch(state, byDistanceSearch) {
            const currentByDistanceSearch = state.byDistanceSearch;
            return {...state, byDistanceSearch: {...currentByDistanceSearch, ...byDistanceSearch}};
        },
        setByDistanceRadius(state, radius) {
            const currentByDistanceSearch = state.byDistanceSearch;
            return {...state, byDistanceSearch: {...currentByDistanceSearch, radius}};
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

const Search = Component(
    {
        submitForm() {
            let locationSearch;
            switch (this.props.search.locationSearchType) {
                case 'byPlace':
                    locationSearch = {type: 'byPlace', geoNodes: this.props.search.byPlaceSearch.selectedLocations.map(loc => parseInt(loc.id, 10))};
                    break;
                case 'byDistance':
                    locationSearch = {type: 'byDistance', geoNode: parseInt(this.props.search.byDistanceSearch.selectedLocation.id, 10), radius: this.props.search.byDistanceSearch.radius};
                    break;
                default:
                    locationSearch = null;
                    break;
            }
            Actions.submitSearch(
                {
                    priceFilter: {max: parseFloat(this.props.search.maxTotalPrice)},
                    roomAmountFilter: {min: parseFloat(this.props.search.minRooms)},
                    surfaceFilter: {min: parseFloat(this.props.search.minSquare)},
                    locationSearch: locationSearch
                }
            )
        },

        render() {
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
                            <ControlLabel>Location</ControlLabel>
                            <LocationsTabPanel/>
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
            search: state.search
        };
    }
);

export {Search, SearchState};