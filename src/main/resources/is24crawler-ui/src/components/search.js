import React from 'react'
import {Component, State} from 'jumpsuit'

const SearchState = State(
    {
        initial: {
            maxTotalPrice: "550",
            minRooms: "1.5",
            minSquare: "45"
        }
    }
);

const Search = Component(
    {
        render() {
            return (
                <div>
                    <form>
                        <label htmlFor="maxTotalPriceInput">Max. Total Price:</label>
                        <input id="maxTotalPriceInput" name="maxTotalPrice" type="text" value={this.props.maxTotalPrice}/>
                        <br/>
                        <label htmlFor="minRoomsInput">Min. Rooms:</label>
                        <input id="minRoomsInput" name="minRooms" type="text" value={this.props.minRooms}/>
                        <br/>
                        <label htmlFor="minSquareInput">Min. Square Meters:</label>
                        <input id="minSquareInput" name="minSquare" type="text" value={this.props.minSquare}/>
                        <br/>
                        <button type="button" onClick={() => console.log("clicked!")}>Search</button>
                    </form>
                </div>
            )
        }
    }, (state) => ({
        maxTotalPrice: state.search.maxTotalPrice,
        minRooms: state.search.minRooms,
        minSquare: state.search.minSquare
    })
);

export {Search, SearchState};