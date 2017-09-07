import React from 'react'
import {Component, State, Actions, Effect} from 'jumpsuit'
import {FormGroup, ControlLabel, FormControl, Button} from 'react-bootstrap'
import httpClient from '../httpClient'

const isPositiveNumeric = (n) => {
    return !isNaN(parseFloat(n)) && isFinite(n) && parseFloat(n) >= 0;
};

const SearchState = State(
    {
        initial: {
            maxTotalPrice: '',
            minRooms: '',
            minSquare: ''
        },
        setMaxTotalPrice(state, maxTotalPrice) {
            return maxTotalPrice === '' || isPositiveNumeric(maxTotalPrice) ? {...state, maxTotalPrice} : state;
        },
        setMinRooms(state, minRooms) {
            return minRooms === '' || isPositiveNumeric(minRooms) ? {...state, minRooms} : state;
        },
        setMinSquare(state, minSquare) {
            return minSquare === '' || (isPositiveNumeric(minSquare) && Number.isInteger(parseFloat(minSquare))) ? {...state, minSquare} : state;
        }
    }
);

Effect('submitSearch', (search) => {
    console.log(search);
    httpClient.post('/api/exposes', search).then(res => Actions.setExposes(res.data))
});

const Search = Component(
    {
        submitForm() {
            Actions.submitSearch(
                {
                    maxTotalPrice: parseFloat(this.props.maxTotalPrice),
                    minRooms: parseFloat(this.props.minRooms),
                    minSquare: parseInt(this.props.minSquare, 10)
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
                                value={this.props.maxTotalPrice}
                                placeholder="Enter price in €"
                                onChange={event => Actions.setMaxTotalPrice(event.target.value)}
                            />
                            <ControlLabel htmlFor="minRoomsInput">Min. rooms</ControlLabel>
                            <FormControl
                                id="minRoomsInput"
                                type="text"
                                value={this.props.minRooms}
                                placeholder="Enter the amount of rooms"
                                onChange={event => Actions.setMinRooms(event.target.value)}
                            />
                            <ControlLabel htmlFor="minSquareInput">Min. square meters</ControlLabel>
                            <FormControl
                                id="minSquareInput"
                                type="text"
                                value={this.props.minSquare}
                                placeholder="Enter the square meters"
                                onChange={event => Actions.setMinSquare(event.target.value)}
                            />
                        </FormGroup>
                        <Button
                            type="submit"
                            onClick={this.submitForm}>
                            Submit
                        </Button>
                    </form>
                </div>
            )
        }
    }, (state) => state.search
);

export {Search, SearchState};