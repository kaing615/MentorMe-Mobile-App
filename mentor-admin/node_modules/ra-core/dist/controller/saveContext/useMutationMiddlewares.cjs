"use strict";
var __spreadArray = (this && this.__spreadArray) || function (to, from, pack) {
    if (pack || arguments.length === 2) for (var i = 0, l = from.length, ar; i < l; i++) {
        if (ar || !(i in from)) {
            if (!ar) ar = Array.prototype.slice.call(from, 0, i);
            ar[i] = from[i];
        }
    }
    return to.concat(ar || Array.prototype.slice.call(from));
};
Object.defineProperty(exports, "__esModule", { value: true });
exports.useMutationMiddlewares = void 0;
var react_1 = require("react");
/**
 * Internal hook used to handle mutation middlewares.
 *
 * @example
 * // We have a form creating an order for a new customer.
 * // The form contains the customer fields in addition to the order fields
 * // but they should be saved as a new customer resource record
 * // and the order should only reference this new customer
 * type Order = { id: string; reference: string };
 * type OrderCreateFormData = { id: string; reference: string; customer: Customer };
 * type Customer = { id: string; email: string; firstName: string; lastName: string };
 *
 * const CustomerForm = props => {
 *     const [createCustomer] = useCreate<Customer>();
 *     const middleware: Middleware<UseCreateResult<OrderCreateFormData>[0]> = useCallback(async (resource, params, next) => {
 *         const { data } = params;
 *         const { user, ...orderData } = data;
 *         const { data = newCustomer } = await createCustomer('customers', { data: user });
 *         const orderDataWithCustomer = { ...orderData, customerId: newCustomer.id };
 *         next(resource, { data: orderDataWithCustomer });
 *     }, [createCustomer]);
 *     useRegisterMutationMiddleware(middleware);
 *
 *     return (
 *         <>
 *             <TextInput source="user.email" />
 *             <TextInput source="user.firstName" />
 *             <TextInput source="user.lastName" />
 *         </>
 *     );
 * }
 */
var useMutationMiddlewares = function () {
    var callbacks = (0, react_1.useRef)([]);
    var registerMutationMiddleware = (0, react_1.useCallback)(function (callback) {
        callbacks.current.push(callback);
    }, []);
    var unregisterMutationMiddleware = (0, react_1.useCallback)(function (callback) {
        callbacks.current = callbacks.current.filter(function (cb) { return cb !== callback; });
    }, []);
    var getMutateWithMiddlewares = (0, react_1.useCallback)(function (fn) {
        // Stores the current callbacks in a closure to avoid losing them if the calling component is unmounted
        var currentCallbacks = __spreadArray([], callbacks.current, true);
        return function () {
            var args = [];
            for (var _i = 0; _i < arguments.length; _i++) {
                args[_i] = arguments[_i];
            }
            var index = currentCallbacks.length - 1;
            // Called by middlewares to call the next middleware function
            // Should take the same arguments as the original mutation function
            var next = function () {
                var newArgs = [];
                for (var _i = 0; _i < arguments.length; _i++) {
                    newArgs[_i] = arguments[_i];
                }
                // Decrement the middlewares counter so that when next is called again, we
                // call the next middleware
                index--;
                // If there are no more middlewares, we call the original mutation function
                if (index >= 0) {
                    return currentCallbacks[index].apply(currentCallbacks, __spreadArray(__spreadArray([], newArgs, false), [next], false));
                }
                else {
                    return fn.apply(void 0, newArgs);
                }
            };
            if (currentCallbacks.length > 0) {
                // Call the first middleware with the same args as the original mutation function
                // with an additional next function
                return currentCallbacks[index].apply(currentCallbacks, __spreadArray(__spreadArray([], args, false), [next], false));
            }
            return fn.apply(void 0, args);
        };
    }, []);
    var functions = (0, react_1.useMemo)(function () { return ({
        registerMutationMiddleware: registerMutationMiddleware,
        getMutateWithMiddlewares: getMutateWithMiddlewares,
        unregisterMutationMiddleware: unregisterMutationMiddleware,
    }); }, [
        registerMutationMiddleware,
        getMutateWithMiddlewares,
        unregisterMutationMiddleware,
    ]);
    return functions;
};
exports.useMutationMiddlewares = useMutationMiddlewares;
//# sourceMappingURL=useMutationMiddlewares.js.map