import React from 'react'
import {Component} from 'jumpsuit'
import {Search} from './search'
import {Results} from './results'

export default Component(
    {
        render() {
            return (
                <div>
                    <h1 style={{textAlign: "center"}}>is24crawler</h1>
                    <Search/>
                    <Results/>
                </div>
            )
        }
    }
);
