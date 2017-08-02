// Import Jumpsuit
import React from 'react'
import {Render, State, Actions, Component, Effect} from 'jumpsuit'
import registerServiceWorker from './registerServiceWorker';

// Create a state with some actions
const CounterState = State({
                               // Initial State
                               initial: {count: 0},
                               // Actions
                               increment: ({count}) => ({count: count + 1}),
                               decrement: ({count}) => ({count: count - 1})
                           });

// Create an async action
Effect('incrementAsync', () => {
    setTimeout(() => Actions.increment(), 1000)
});

// Create a subscribed component
const Counter = Component({
                              render() {
                                  return (
                                      <div>
                                          <h1>{this.props.count}</h1>
                                          <button onClick={Actions.increment}>Increment</button>
                                          <button onClick={Actions.decrement}>Decrement</button>
                                          <br/>
                                          <button onClick={Actions.incrementAsync}>Increment Async</button>
                                      </div>
                                  )
                              }
                          }, (state) => ({
    // Subscribe to the counter state (will be available via this.props.counter)
    count: state.counter.count
}));

// Compose the global state
const globalState = {counter: CounterState};

// Render your app!
Render(globalState, <Counter/>, 'root');

// registerServiceWorker();
